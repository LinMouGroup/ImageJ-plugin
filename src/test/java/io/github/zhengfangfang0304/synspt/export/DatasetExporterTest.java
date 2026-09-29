package io.github.zhengfangfang0304.synspt.export;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.engine.RenderedSimulationResult;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancellationToken;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancelledException;
import io.github.zhengfangfang0304.synspt.engine.SimulationProgressListener;
import io.github.zhengfangfang0304.synspt.engine.SyntheticDatasetGenerator;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.optical.PsfShapeType;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;

import ij.ImagePlus;
import ij.io.Opener;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DatasetExporterTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private io.github.zhengfangfang0304.synspt.run.SimulationRunContext newRun(
            Path base, RenderedSimulationResult result) throws IOException {
        return new io.github.zhengfangfang0304.synspt.run.SimulationRunDirectoryFactory(
                java.time.Clock.fixed(java.time.Instant.parse("2026-09-15T00:45:01Z"),
                        java.time.ZoneId.of("Asia/Shanghai")))
                .create(base, result.getPlan().getRequestConfig());
    }

    @Test
    public void defaultExportWritesOnlyThreeFilesAndMatchingRunIdentity() throws Exception {
        Path base = temporaryFolder.newFolder("default-dataset").toPath();
        RenderedSimulationResult result = result(70707L);
        io.github.zhengfangfang0304.synspt.run.SimulationRunContext run = newRun(base, result);
        ExportManifest manifest = new DatasetExporter().export(result, run,
                SimulationProgressListener.NONE, SimulationCancellationToken.NONE);
        assertEquals(run.getDirectory(), manifest.getRunDirectory());
        assertEquals(run.getRunId(), manifest.getRunId());
        assertEquals(1, countChildren(base));
        assertEquals(3, countChildren(run.getDirectory()));
        assertFalse(manifest.hasAdvancedExports());
        assertFalse(Files.exists(run.getDirectory().resolve("advanced")));
        assertTiffPixelsAndCalibrationUnchanged(result, manifest.getTiffPath());
        assertDefaultTrajectory(result, manifest.getTrajectoryCsvPath());
        String metadata = read(manifest.getMetadataJsonPath());
        assertCompleteMetadata(metadata, 70707L);
        for (int i = 0; i < result.getPlan().getParticles().size(); i++) {
            String particleJson = metadata.substring(metadata.indexOf("\"particles\": ["))
                    .split("\"particle_id\": ")[i + 1];
            io.github.zhengfangfang0304.synspt.optical.ParticleAppearanceSample sample =
                    result.getPlan().getAppearancePlan().getSamples().get(i);
            io.github.zhengfangfang0304.synspt.optical.ParticlePsfParameters psf = sample.getPsfParameters();
            assertTrue(particleJson.contains("\"psf_size_class\": \"" + psf.getSizeClass().name() + "\""));
            assertTrue(particleJson.contains("\"sigma_x\": " + Double.toString(psf.getSigmaXPixels())));
            assertTrue(particleJson.contains("\"sigma_y\": " + Double.toString(psf.getSigmaYPixels())));
            assertTrue(particleJson.contains("\"aspect_ratio\": " + Double.toString(psf.getSigmaYPixels() / psf.getSigmaXPixels())));
            assertTrue(particleJson.contains("\"rotation_angle\": " + Double.toString(psf.getRotationRadians())));
            assertTrue(particleJson.contains("\"brightness_weight\": " + Double.toString(sample.getBrightnessWeight())));
            assertTrue(particleJson.contains("\"sigma_unit\": \"pixel\""));
            assertTrue(particleJson.contains("\"rotation_angle_unit\": \"rad\""));
        }
        assertTrue(metadata.contains("\"schema_version\": \"10.1\""));
        assertTrue(metadata.contains("\"run_id\": \"" + run.getRunId() + "\""));
        assertTrue(metadata.contains("\"selected_motion_models\": [\"BROWNIAN\", \"MULTI_STATE\"]"));
        assertTrue(run.getRunId().contains("_MIXED_seed70707"));
        assertFalse(metadata.contains("\"random_seed\""));
        assertFalse(metadata.contains("\"root_seed_decimal\""));
        assertTrue(metadata.contains("\"advanced_export\": false"));
        assertNoTransactionFiles(base);
    }

    @Test
    public void advancedExportRemainsInsideItsOwnRun() throws Exception {
        Path base = temporaryFolder.newFolder("advanced-dataset").toPath();
        RenderedSimulationResult result = result(71717L);
        io.github.zhengfangfang0304.synspt.run.SimulationRunContext run = newRun(base, result);
        ExportManifest manifest = new DatasetExporter(ExportConfig.advancedExport()).export(
                result, run, SimulationProgressListener.NONE, SimulationCancellationToken.NONE);
        assertEquals(4, countChildren(run.getDirectory()));
        assertEquals(3, manifest.getAdvancedPaths().size());
        for (Path path : manifest.getAdvancedPaths()) {
            assertEquals(run.getDirectory().resolve("advanced"), path.getParent());
        }
        assertTrajectoryStates(result, manifest.getAdvancedPaths().get(0));
        assertParticleProperties(result, manifest.getAdvancedPaths().get(1));
        assertTrajectorySegments(result, manifest.getAdvancedPaths().get(2));
        assertCompleteMetadata(read(manifest.getMetadataJsonPath()), 71717L);
        assertNoTransactionFiles(base);
    }

    @Test
    public void mixedBasicModelsAreRecordedCompletely() throws Exception {
        SimulationConfig config = SimulationConfig.builder().imageWidth(12).imageHeight(10)
                .frames(30).particleNumber(3).randomSeed(Long.MIN_VALUE)
                .selectedMotionTypes(EnumSet.of(MotionType.BROWNIAN, MotionType.DIRECTED, MotionType.FBM)).build();
        io.github.zhengfangfang0304.synspt.run.SimulationRunContext run =
                new io.github.zhengfangfang0304.synspt.run.SimulationRunDirectoryFactory().create(
                        temporaryFolder.newFolder().toPath(), config);
        RenderedSimulationResult result = new SyntheticDatasetGenerator().generate(run.getConfig(), SimulationProgressListener.NONE);
        ExportManifest manifest = new DatasetExporter().export(result, run,
                SimulationProgressListener.NONE, SimulationCancellationToken.NONE);
        String metadata = read(manifest.getMetadataJsonPath());
        assertTrue(run.getRunId().contains("_MIXED_seed-9223372036854775808"));
        assertTrue(metadata.contains("\"selected_motion_models\": [\"BROWNIAN\", \"DIRECTED\", \"FBM\"]"));
        assertTrue(metadata.contains("\"root_seed\": -9223372036854775808"));
        assertTrue(metadata.contains("\"run_id\": \"" + run.getDirectory().getFileName() + "\""));
    }

    @Test
    public void repeatedRunsPreserveExistingDataAndProduceIdenticalTrajectories() throws Exception {
        Path base = temporaryFolder.newFolder("repeated").toPath();
        Files.write(base.resolve("trajectory.csv"), "legacy-data".getBytes(StandardCharsets.UTF_8));
        RenderedSimulationResult first = result(80808L);
        io.github.zhengfangfang0304.synspt.run.SimulationRunContext a = newRun(base, first);
        DatasetExporter exporter = new DatasetExporter();
        ExportManifest one = exporter.export(first, a, SimulationProgressListener.NONE, SimulationCancellationToken.NONE);
        byte[] original = Files.readAllBytes(one.getTrajectoryCsvPath());
        // Reserve the second run before generation, as Generate does.
        io.github.zhengfangfang0304.synspt.run.SimulationRunContext b = newRun(base, first);
        RenderedSimulationResult second = new SyntheticDatasetGenerator().generate(
                b.getConfig(), SimulationProgressListener.NONE);
        ExportManifest two = exporter.export(second, b, SimulationProgressListener.NONE, SimulationCancellationToken.NONE);
        assertEquals(a.getRunId() + "_2", b.getRunId());
        assertArrayEquals(original, Files.readAllBytes(one.getTrajectoryCsvPath()));
        assertArrayEquals(original, Files.readAllBytes(two.getTrajectoryCsvPath()));
        assertEquals("legacy-data", read(base.resolve("trajectory.csv")));
        assertTrue(read(two.getMetadataJsonPath()).contains("\"run_id\": \"" + b.getRunId() + "\""));
        assertTiffPixelsAndCalibrationUnchanged(first, two.getTiffPath());
        try {
            exporter.export(first, a, SimulationProgressListener.NONE, SimulationCancellationToken.NONE);
            org.junit.Assert.fail("Reusing a run must fail");
        } catch (IllegalStateException expected) {
            assertArrayEquals(original, Files.readAllBytes(one.getTrajectoryCsvPath()));
        }
    }

    @Test
    public void fileAppearingDuringExportIsNotOverwrittenAndOwnFilesRollBack() throws Exception {
        Path base = temporaryFolder.newFolder("collision").toPath();
        RenderedSimulationResult result = result(101L);
        final io.github.zhengfangfang0304.synspt.run.SimulationRunContext run = newRun(base, result);
        final Path existing = run.getDirectory().resolve("trajectory.csv");
        try {
            new DatasetExporter().export(result, run, new SimulationProgressListener() {
                @Override public void onProgress(int percent, String message) {
                    if (percent == 92) {
                        try { Files.write(existing, "do-not-replace".getBytes(StandardCharsets.UTF_8)); }
                        catch (IOException e) { throw new RuntimeException(e); }
                    }
                }
            }, SimulationCancellationToken.NONE);
            org.junit.Assert.fail("Existing file must reject export");
        } catch (java.nio.file.FileAlreadyExistsException expected) {
            assertEquals("do-not-replace", read(existing));
            assertEquals(1, countChildren(run.getDirectory()));
            assertNoTransactionFiles(base);
        }
    }

    @Test
    public void cancellationKeepsReservedRunAndLeavesNoOutputFiles() throws Exception {
        Path base = temporaryFolder.newFolder("cancelled").toPath();
        RenderedSimulationResult result = result(90909L);
        io.github.zhengfangfang0304.synspt.run.SimulationRunContext run = newRun(base, result);
        try {
            new DatasetExporter().export(result, run, SimulationProgressListener.NONE,
                    new SimulationCancellationToken() {
                        @Override public boolean isCancellationRequested() { return true; }
                    });
            org.junit.Assert.fail("Cancellation expected");
        } catch (SimulationCancelledException expected) {
            assertEquals(0, countChildren(run.getDirectory()));
            assertEquals(run.getRunId() + "_2", newRun(base, result).getRunId());
        }
    }

    @Test
    public void mismatchedSimulationCannotBeExportedUnderAnotherRunIdentity() throws Exception {
        Path base = temporaryFolder.newFolder("wrong-config").toPath();
        RenderedSimulationResult result = result(123L);
        io.github.zhengfangfang0304.synspt.run.SimulationRunContext run = newRun(base, result);
        try {
            new DatasetExporter().export(result(124L), run, SimulationProgressListener.NONE,
                    SimulationCancellationToken.NONE);
            org.junit.Assert.fail("Mismatched config must fail");
        } catch (IllegalArgumentException expected) {
            assertEquals(0, countChildren(run.getDirectory()));
        }
    }
    private void assertDefaultTrajectory(
            RenderedSimulationResult result,
            Path trajectoryPath
    ) throws IOException {
        List<String> lines = Files.readAllLines(
                trajectoryPath,
                StandardCharsets.UTF_8
        );
        assertEquals(TrajectoryCsvExporter.HEADER, lines.get(0));
        assertEquals(1 + 2 * 30, lines.size());
        int row = 1;
        for (ParticleTrack track : result.getScene().getParticleTracks()) {
            for (int frame = 0; frame < track.getTrajectory().length(); frame++) {
                String[] values = lines.get(row++).split(",");
                assertEquals(9, values.length);
                assertEquals(track.getParticle().getParticleId(),
                        Integer.parseInt(values[0]));
                assertEquals(frame, Integer.parseInt(values[1]));
                assertEquals(frame * result.getPlan().getRequestConfig()
                        .getMicroscopeConfig().getFrameIntervalSeconds(),
                        Double.parseDouble(values[2]), 0.0);
                assertEquals(track.getTrajectory().getXUm(frame),
                        Double.parseDouble(values[3]), 0.0);
                assertEquals(track.getTrajectory().getYUm(frame),
                        Double.parseDouble(values[4]), 0.0);
                io.github.zhengfangfang0304.synspt.motion.StateSegment segment =
                        track.getParticle().getStateSegmentForFrame(frame);
                assertEquals(segment.getStateIndex(), Integer.parseInt(values[5]));
                assertEquals(segment.getMotionType().name(), values[6]);
                assertEquals(Boolean.toString(segment.getStateIndex() > 0
                        && frame == segment.getStartFrame()), values[7]);
                assertEquals(MotionParametersFormatter.format(segment.getMotionType(),
                        segment.getMotionParameters()), values[8]);
            }
        }
    }

    private void assertTrajectoryStates(
            RenderedSimulationResult result,
            Path statesPath
    ) throws IOException {
        List<String> lines = Files.readAllLines(statesPath, StandardCharsets.UTF_8);
        assertEquals(
                "particle_id,state_id,motion_type,start_frame,end_frame,parameters",
                lines.get(0)
        );
        int row = 1;
        for (Particle particle : result.getPlan().getParticles()) {
            for (io.github.zhengfangfang0304.synspt.motion.StateSegment state
                    : particle.getStateSegments()) {
                String[] values = lines.get(row++).split(",", 6);
                assertEquals(particle.getParticleId(), Integer.parseInt(values[0]));
                assertEquals(state.getStateIndex(), Integer.parseInt(values[1]));
                assertEquals(state.getMotionType().name(), values[2]);
                assertEquals(state.getStartFrame(), Integer.parseInt(values[3]));
                assertEquals(state.getEndFrame(), Integer.parseInt(values[4]));
                assertEquals(parameterText(state), values[5]);
            }
        }
        assertEquals(row, lines.size());
    }

    private void assertParticleProperties(
            RenderedSimulationResult result,
            Path propertiesPath
    ) throws IOException {
        List<String> lines = Files.readAllLines(
                propertiesPath,
                StandardCharsets.UTF_8
        );
        assertEquals(
                "particle_id,brightness_photons_per_frame,psf_shape,"
                        + "psf_size_class,radius_pixels,sigma_x_pixels,sigma_y_pixels,"
                        + "rotation_radians,motion_parameters",
                lines.get(0)
        );
        assertEquals(1 + result.getPlan().getParticles().size(), lines.size());
        for (int index = 0; index < result.getPlan().getParticles().size(); index++) {
            Particle particle = result.getPlan().getParticles().get(index);
            String[] values = lines.get(index + 1).split(",", 9);
            assertEquals(particle.getParticleId(), Integer.parseInt(values[0]));
            assertEquals(particle.getImagingParameters().getEmitterPhotonsPerFrame(),
                    Double.parseDouble(values[1]), 0.0);
            assertEquals(particle.getImagingParameters().getPsfParameters()
                    .getShapeType().name(), values[2]);
            assertEquals(particle.getImagingParameters().getPsfParameters()
                    .getSizeClass().name(), values[3]);
            assertTrue(values[8].startsWith(
                    particle.getStateSegments().get(0).getMotionType().name()
                            + "{"
            ));
            assertTrue(values[8].contains("="));
        }
    }

    private void assertTrajectorySegments(
            RenderedSimulationResult result,
            Path segmentsPath
    ) throws IOException {
        List<String> lines = Files.readAllLines(segmentsPath, StandardCharsets.UTF_8);
        assertEquals("particle_id,start_frame,end_frame,motion_state", lines.get(0));
        int row = 1;
        for (Particle particle : result.getPlan().getParticles()) {
            for (io.github.zhengfangfang0304.synspt.motion.StateSegment state
                    : particle.getStateSegments()) {
                String[] values = lines.get(row++).split(",");
                assertEquals(particle.getParticleId(),
                        Integer.parseInt(values[0]));
                assertEquals(state.getStartFrame(),
                        Integer.parseInt(values[1]));
                assertEquals(state.getEndFrame(),
                        Integer.parseInt(values[2]));
                assertEquals(state.getMotionType().name(), values[3]);
            }
        }
        assertEquals(row, lines.size());
    }

    private String parameterText(
            io.github.zhengfangfang0304.synspt.motion.StateSegment state
    ) {
        StringBuilder text = new StringBuilder();
        for (java.util.Map.Entry<String, String> entry
                : state.getMotionParameters().toMetadata().entrySet()) {
            if (text.length() > 0) {
                text.append(';');
            }
            text.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return text.toString();
    }

    private void assertTiffPixelsAndCalibrationUnchanged(
            RenderedSimulationResult result,
            Path tiffPath
    ) {
        ImagePlus reopened = new Opener().openImage(tiffPath.toString());
        assertNotNull(reopened);
        assertEquals(12, reopened.getWidth());
        assertEquals(10, reopened.getHeight());
        assertEquals(30, reopened.getStackSize());
        assertEquals(result.getImagePlus().getBitDepth(), reopened.getBitDepth());
        for (int slice = 1; slice <= reopened.getStackSize(); slice++) {
            assertArrayEquals(
                    (short[]) result.getImagePlus().getStack().getPixels(slice),
                    (short[]) reopened.getStack().getPixels(slice)
            );
        }
        assertEquals(0.1, reopened.getCalibration().pixelWidth, 1.0e-12);
        assertEquals(1.0 / 30.0,
                reopened.getCalibration().frameInterval, 1.0e-12);
    }

    private void assertCompleteMetadata(String metadata, long seed) {
        assertTrue(metadata.startsWith("{"));
        assertTrue(metadata.trim().endsWith("}"));
        assertTrue(metadata.contains("\"generator\": \"synSPT Synthetic Data Generator\""));
        assertTrue(metadata.contains("\"root_seed\": " + seed));
        assertTrue(metadata.contains("\"pixel_size_um\": 0.1"));
        assertTrue(metadata.contains("\"frame_interval_seconds\": "
                + Double.toString(1.0 / 30.0)));
        assertTrue(metadata.contains("\"frame_rate_fps\": 30.0"));
        assertTrue(metadata.contains("\"shot_noise\": \"poisson\""));
        assertTrue(metadata.contains("\"target_snr\": 10.0"));
        assertTrue(metadata.contains("\"calibration_method\": \"photon_budget\""));
        assertTrue(metadata.contains(
                "\"snr_definition\": "
                        + "\"peak_pixel_signal_over_total_noise\""
        ));
        assertTrue(metadata.contains("\"reference_photon_budget\""));
        assertTrue(metadata.contains("\"photon_scale\""));
        assertTrue(metadata.contains("\"predicted_snr\""));
        assertTrue(metadata.contains("\"realized_snr_distribution\""));
        assertTrue(metadata.contains("\"physical_read_noise_sigma_adu\": 2.0"));
        assertFalse(metadata.contains("additional_gaussian"));
        assertFalse(metadata.contains("additional_noise"));
        assertTrue(metadata.contains("\"motion_composition\""));
        assertTrue(metadata.contains("\"sampling_weight\""));
        assertTrue(metadata.contains("\"particle_count\""));
        assertTrue(metadata.contains("\"motion_parameter_distribution\""));
        assertTrue(metadata.contains("\"appearance_distribution\""));
        assertTrue(metadata.contains("\"particles\""));
        assertTrue(metadata.contains("\"relative_brightness\""));
        assertTrue(metadata.contains("\"emitter_photons_per_frame\""));
        assertTrue(metadata.contains("\"aspect_ratio\""));
        assertTrue(metadata.contains("\"multi_state\""));
        assertTrue(metadata.contains("\"consecutive_repeat_weight\": 0.25"));
        assertTrue(metadata.contains("\"reproducibility\""));
        assertTrue(metadata.contains(
                "\"seed_derivation\": "
                        + "\"io.github.zhengfangfang0304.synspt.util.SeedDerivation\""
        ));
        assertTrue(metadata.contains("\"camera_noise_seeds_by_frame\""));
        assertTrue(metadata.contains("\"generator\": \"bounded_dirichlet\""));
        assertTrue(metadata.contains("\"size_class\": \"SMALL\""));
        assertTrue(metadata.contains("\"size_class\": \"MEDIUM\""));
        assertTrue(metadata.contains("\"size_class\": \"LARGE\""));
        assertTrue(metadata.contains("\"dirichlet_alpha\": 9.0"));
        assertTrue(metadata.contains("\"sampled_ratio\""));
        assertTrue(metadata.contains("\"rho\": 0.25"));
        assertTrue(metadata.contains("\"image\": \"simulation.tif\""));
        assertTrue(metadata.contains("\"trajectory\": \"trajectory.csv\""));
        assertTrue(metadata.contains("\"metadata\": \"metadata.json\""));
        assertFalse(metadata.contains(",\n  }"));
    }

    private RenderedSimulationResult result(long seed) {
        SimulationConfig config = SimulationConfig.builder()
                .imageWidth(12)
                .imageHeight(10)
                .frames(30)
                .particleNumber(2)
            .selectedMotionTypes(EnumSet.of(
                    MotionType.BROWNIAN,
                    MotionType.MULTI_STATE
            ))
            .spotShape(PsfShapeType.ELLIPTICAL_GAUSSIAN)
                .randomSeed(seed)
                .build();
        return new SyntheticDatasetGenerator().generate(
                config,
                SimulationProgressListener.NONE
        );
    }

    private String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private int countChildren(Path directory) throws IOException {
        int count = 0;
        DirectoryStream<Path> stream = Files.newDirectoryStream(directory);
        try {
            for (Path ignored : stream) {
                count++;
            }
        } finally {
            stream.close();
        }
        return count;
    }

    private void assertNoTransactionFiles(Path directory) throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(directory);
        try {
            for (Path path : stream) {
                assertFalse(path.getFileName().toString().startsWith(".synspt-"));
                if (Files.isDirectory(path)) {
                    assertNoTransactionFiles(path);
                }
            }
        } finally {
            stream.close();
        }
    }
}
