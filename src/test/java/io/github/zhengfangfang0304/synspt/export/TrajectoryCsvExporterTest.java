package io.github.zhengfangfang0304.synspt.export;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.engine.*;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;
import io.github.zhengfangfang0304.synspt.motion.parameters.BrownianParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.DirectedParameters;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;
import io.github.zhengfangfang0304.synspt.scene.Scene;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import static org.junit.Assert.*;

public class TrajectoryCsvExporterTest {
    private static final String HEADER = "particle_id,frame,time_s,x_um,y_um,segment_id,"
            + "motion_type,is_change_point,motion_parameters";
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void singleBrownianUsesOneSegmentAndPhysicalTime() throws Exception {
        RenderedSimulationResult result = fixture(false);
        List<String> lines = lines(result);
        assertEquals(HEADER, lines.get(0));
        assertEquals(101, lines.size());
        for (int frame = 0; frame < 100; frame++) {
            String[] row = lines.get(frame + 1).split(",", -1);
            assertEquals(9, row.length);
            assertEquals("1", row[0]);
            assertEquals(Integer.toString(frame), row[1]);
            assertEquals(frame / 30.0, Double.parseDouble(row[2]), 1e-12);
            assertEquals("0", row[5]);
            assertEquals("BROWNIAN", row[6]);
            assertEquals("false", row[7]);
            assertEquals("D=1.0 um^2/s", row[8]);
        }
        assertEquals(1.0, Double.parseDouble(lines.get(31).split(",")[2]), 1e-12);
    }

    @Test
    public void sameModelParameterChangeAndDifferentModelUseSavedSegments() throws Exception {
        List<String> lines = lines(fixture(true));
        assertEquals(HEADER, lines.get(0));
        for (int frame = 0; frame < 100; frame++) {
            String[] row = lines.get(frame + 1).split(",", -1);
            int segment = frame <= 30 ? 0 : frame <= 60 ? 1 : 2;
            assertEquals(Integer.toString(segment), row[5]);
            assertEquals(segment < 2 ? "BROWNIAN" : "DIRECTED", row[6]);
            assertEquals(Boolean.toString(frame == 31 || frame == 61), row[7]);
            assertEquals(segment == 0 ? "D=1.0 um^2/s" : segment == 1
                    ? "D=0.2 um^2/s" : "D=0.1 um^2/s; speed=1.5 um/s; direction=0.5 rad", row[8]);
            // Identical positions intentionally prevent inferring the model from behavior.
            assertEquals("0.0", row[3]);
            assertEquals("0.0", row[4]);
        }
    }

    @Test
    public void fixedSeedReproducesCsvBytes() throws Exception {
        SimulationConfig config = config(MotionType.MULTI_STATE);
        RenderedSimulationResult a = new SyntheticDatasetGenerator().generate(config, SimulationProgressListener.NONE);
        RenderedSimulationResult b = new SyntheticDatasetGenerator().generate(config, SimulationProgressListener.NONE);
        assertArrayEquals(Files.readAllBytes(write(a)), Files.readAllBytes(write(b)));
    }

    @Test
    public void csvEscapesCommasQuotesAndBothNewlineCharacters() {
        assertEquals("D=1.0 um^2/s; alpha=0.5", TrajectoryCsvExporter.csvField("D=1.0 um^2/s; alpha=0.5"));
        assertEquals("\"a,b\"", TrajectoryCsvExporter.csvField("a,b"));
        assertEquals("\"a\"\"b\"", TrajectoryCsvExporter.csvField("a\"b"));
        assertEquals("\"a\nb\"", TrajectoryCsvExporter.csvField("a\nb"));
        assertEquals("\"a\rb\"", TrajectoryCsvExporter.csvField("a\rb"));
    }

    private RenderedSimulationResult fixture(boolean multi) {
        RenderedSimulationResult base = new SyntheticDatasetGenerator().generate(
                config(multi ? MotionType.MULTI_STATE : MotionType.BROWNIAN), SimulationProgressListener.NONE);
        List<StateSegment> segments = multi ? Arrays.asList(
                new StateSegment(1, 0, 0, 30, MotionType.BROWNIAN, new BrownianParameters(1.0)),
                new StateSegment(1, 1, 31, 60, MotionType.BROWNIAN, new BrownianParameters(0.2)),
                new StateSegment(1, 2, 61, 99, MotionType.DIRECTED, new DirectedParameters(0.1, 1.5, 0.5)))
                : Collections.singletonList(new StateSegment(1, 0, 0, 99,
                        MotionType.BROWNIAN, new BrownianParameters(1.0)));
        Particle particle = new Particle(1, multi ? MotionType.MULTI_STATE : MotionType.BROWNIAN,
                segments, base.getPlan().getParticles().get(0).getImagingParameters());
        SimulationPlan plan = new SimulationPlan(base.getPlan().getRequestConfig(),
                base.getPlan().getMotionComposition(), base.getPlan().getAppearancePlan(),
                Collections.singletonList(particle), base.getPlan().getPhotonCalibration());
        Scene scene = new Scene(12, 10, plan.getRequestConfig().getMicroscopeConfig().getPixelSizeUm(),
                Collections.singletonList(new ParticleTrack(particle, new Trajectory(new double[100], new double[100]))));
        return new RenderedSimulationResult(new SimulationResult(plan, scene), base.getImagePlus());
    }

    private SimulationConfig config(MotionType type) {
        return SimulationConfig.builder().imageWidth(12).imageHeight(10).frames(100)
                .particleNumber(1).selectedMotionTypes(EnumSet.of(type)).randomSeed(123456L).build();
    }

    private List<String> lines(RenderedSimulationResult result) throws Exception {
        return Files.readAllLines(write(result), StandardCharsets.UTF_8);
    }

    private Path write(RenderedSimulationResult result) throws Exception {
        Path path = temporaryFolder.newFile().toPath();
        new TrajectoryCsvExporter().write(result, path, SimulationCancellationToken.NONE);
        return path;
    }
}
