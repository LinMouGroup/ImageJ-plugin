package io.github.zhengfangfang0304.synspt.export;

import io.github.zhengfangfang0304.synspt.engine.RenderedSimulationResult;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancellationToken;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancelledException;
import io.github.zhengfangfang0304.synspt.engine.SimulationProgressListener;
import io.github.zhengfangfang0304.synspt.run.SimulationRunContext;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Export into a reserved run, never replacing existing files; optional advanced bundle. */
public final class DatasetExporter {

    public static final String TIFF_FILE_NAME = "simulation.tif";
    public static final String TRAJECTORY_FILE_NAME = "trajectory.csv";
    public static final String METADATA_FILE_NAME = "metadata.json";
    public static final String ADVANCED_DIRECTORY_NAME = "advanced";
    public static final String TRAJECTORY_STATES_FILE_NAME =
            "trajectory_states.csv";
    public static final String PARTICLE_PROPERTIES_FILE_NAME =
            "particle_properties.csv";
    public static final String TRAJECTORY_SEGMENTS_FILE_NAME =
            "trajectory_segments.csv";

    private final ExportConfig exportConfig;
    private final TiffStackExporter tiffExporter;
    private final TrajectoryCsvExporter trajectoryExporter;
    private final TrajectoryStatesCsvExporter trajectoryStatesExporter;
    private final ParticlePropertiesCsvExporter particlePropertiesExporter;
    private final TrajectorySegmentsCsvExporter trajectorySegmentsExporter;
    private final MetadataJsonExporter metadataExporter;

    public DatasetExporter() {
        this(ExportConfig.defaultExport());
    }

    public DatasetExporter(ExportConfig exportConfig) {
        if (exportConfig == null) {
            throw new IllegalArgumentException("Export config cannot be null.");
        }
        this.exportConfig = exportConfig;
        this.tiffExporter = new TiffStackExporter();
        this.trajectoryExporter = new TrajectoryCsvExporter();
        this.trajectoryStatesExporter = new TrajectoryStatesCsvExporter();
        this.particlePropertiesExporter = new ParticlePropertiesCsvExporter();
        this.trajectorySegmentsExporter = new TrajectorySegmentsCsvExporter();
        this.metadataExporter = new MetadataJsonExporter();
    }

    public ExportManifest export(
            RenderedSimulationResult result,
            SimulationRunContext run,
            SimulationProgressListener progressListener,
            SimulationCancellationToken cancellationToken
    ) throws IOException {
        if (run == null) { throw new IllegalArgumentException("Run context is required."); }
        Path outputDirectory = run.getDirectory();
        validateArguments(
                result,
                outputDirectory,
                progressListener,
                cancellationToken
        );
        Path normalizedDirectory = outputDirectory.toAbsolutePath().normalize();
        Path tiffTarget = normalizedDirectory.resolve(TIFF_FILE_NAME);
        Path trajectoryTarget = normalizedDirectory.resolve(TRAJECTORY_FILE_NAME);
        Path metadataTarget = normalizedDirectory.resolve(METADATA_FILE_NAME);
        Path advancedDirectory = normalizedDirectory.resolve(
                ADVANCED_DIRECTORY_NAME
        );
        List<Path> advancedTargets = advancedTargets(advancedDirectory);
        List<StagedFile> stagedFiles = new ArrayList<StagedFile>();
        boolean advancedDirectoryCreated = false;

        progressListener.onProgress(0, "Preparing dataset export");
        run.beginExport(result.getPlan().getRequestConfig());
        checkCancellation(cancellationToken);
        try {
            StagedFile tiff = stage(normalizedDirectory, tiffTarget, ".tif");
            stagedFiles.add(tiff);
            tiffExporter.write(result, tiff.temporaryPath);
            progressListener.onProgress(30, "TIFF stack staged");

            checkCancellation(cancellationToken);
            StagedFile trajectory = stage(
                    normalizedDirectory,
                    trajectoryTarget,
                    ".csv"
            );
            stagedFiles.add(trajectory);
            trajectoryExporter.write(
                    result,
                    trajectory.temporaryPath,
                    cancellationToken
            );
            progressListener.onProgress(55, "Trajectory CSV staged");

            if (exportConfig.isExportAdvanced()) {
                stageAdvancedFiles(
                        result,
                        normalizedDirectory,
                        advancedTargets,
                        stagedFiles,
                        progressListener,
                        cancellationToken
                );
            }

            checkCancellation(cancellationToken);
            StagedFile metadata = stage(
                    normalizedDirectory,
                    metadataTarget,
                    ".json"
            );
            stagedFiles.add(metadata);
            metadataExporter.write(
                    result,
                    metadata.temporaryPath,
                    exportConfig,
                    run
            );
            progressListener.onProgress(92, "Metadata JSON staged");

            checkCancellation(cancellationToken);
            if (exportConfig.isExportAdvanced()
                    && Files.notExists(advancedDirectory)) {
                Files.createDirectory(advancedDirectory);
                advancedDirectoryCreated = true;
            }
            commit(stagedFiles);
            progressListener.onProgress(100, "Dataset export complete");
            return new ExportManifest(
                    tiffTarget,
                    trajectoryTarget,
                    metadataTarget,
                    advancedTargets
            );
        } catch (IOException exception) {
            cleanupAfterFailure(
                    stagedFiles,
                    advancedDirectory,
                    advancedDirectoryCreated,
                    exception
            );
            throw exception;
        } catch (RuntimeException exception) {
            cleanupAfterFailure(
                    stagedFiles,
                    advancedDirectory,
                    advancedDirectoryCreated,
                    exception
            );
            throw exception;
        }
    }

    private void stageAdvancedFiles(
            RenderedSimulationResult result,
            Path stagingDirectory,
            List<Path> advancedTargets,
            List<StagedFile> stagedFiles,
            SimulationProgressListener progressListener,
            SimulationCancellationToken cancellationToken
    ) throws IOException {
        checkCancellation(cancellationToken);
        StagedFile states = stage(
                stagingDirectory,
                advancedTargets.get(0),
                ".csv"
        );
        stagedFiles.add(states);
        trajectoryStatesExporter.write(
                result,
                states.temporaryPath,
                cancellationToken
        );
        progressListener.onProgress(65, "Trajectory states CSV staged");

        checkCancellation(cancellationToken);
        StagedFile properties = stage(
                stagingDirectory,
                advancedTargets.get(1),
                ".csv"
        );
        stagedFiles.add(properties);
        particlePropertiesExporter.write(
                result,
                properties.temporaryPath,
                cancellationToken
        );
        progressListener.onProgress(75, "Particle properties CSV staged");

        checkCancellation(cancellationToken);
        StagedFile segments = stage(
                stagingDirectory,
                advancedTargets.get(2),
                ".csv"
        );
        stagedFiles.add(segments);
        trajectorySegmentsExporter.write(
                result,
                segments.temporaryPath,
                cancellationToken
        );
        progressListener.onProgress(85, "Trajectory segments CSV staged");
    }

    private List<Path> advancedTargets(Path advancedDirectory) {
        List<Path> targets = new ArrayList<Path>(3);
        if (exportConfig.isExportAdvanced()) {
            targets.add(advancedDirectory.resolve(TRAJECTORY_STATES_FILE_NAME));
            targets.add(advancedDirectory.resolve(PARTICLE_PROPERTIES_FILE_NAME));
            targets.add(advancedDirectory.resolve(TRAJECTORY_SEGMENTS_FILE_NAME));
        }
        return targets;
    }

    private void validateArguments(
            RenderedSimulationResult result,
            Path outputDirectory,
            SimulationProgressListener progressListener,
            SimulationCancellationToken cancellationToken
    ) {
        if (result == null
                || outputDirectory == null
                || progressListener == null
                || cancellationToken == null) {
            throw new IllegalArgumentException(
                    "Result, output directory, progress listener, and cancellation token "
                            + "cannot be null."
            );
        }
        if (!Files.isDirectory(outputDirectory, LinkOption.NOFOLLOW_LINKS) || !Files.isWritable(outputDirectory)) {
            throw new IllegalArgumentException(
                    "Output location must be an existing writable directory."
            );
        }
    }

    private StagedFile stage(Path directory, Path target, String suffix)
            throws IOException {
        Path temporary = Files.createTempFile(directory, ".synspt-stage-", suffix);
        return new StagedFile(temporary, target);
    }

    private void commit(List<StagedFile> stagedFiles) throws IOException {
        try {
            for (StagedFile stagedFile : stagedFiles) {
                // CREATE_NEW atomically refuses an existing file or link. Do not use
                // ATOMIC_MOVE, whose existing-target behavior is implementation-specific.
                try (OutputStream output = Files.newOutputStream(stagedFile.targetPath,
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                    stagedFile.installed = true;
                    Files.copy(stagedFile.temporaryPath, output);
                }
            }
        } catch (IOException | RuntimeException exception) {
            for (StagedFile stagedFile : stagedFiles) {
                if (stagedFile.installed) {
                    deleteIfExists(stagedFile.targetPath, exception);
                }
            }
            throw exception;
        }
        for (StagedFile stagedFile : stagedFiles) {
            deleteIfExists(stagedFile.temporaryPath, null);
        }
    }

    private void cleanupAfterFailure(
            List<StagedFile> stagedFiles,
            Path advancedDirectory,
            boolean advancedDirectoryCreated,
            Throwable original
    ) {
        cleanupTemporaryFiles(stagedFiles, original);
        if (advancedDirectoryCreated) {
            deleteIfExists(advancedDirectory, original);
        }
    }

    private void cleanupTemporaryFiles(
            List<StagedFile> stagedFiles,
            Throwable original
    ) {
        for (StagedFile stagedFile : stagedFiles) {
            deleteIfExists(stagedFile.temporaryPath, original);
        }
    }

    private void deleteIfExists(Path path, Throwable original) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException cleanupFailure) {
            if (original != null) {
                original.addSuppressed(cleanupFailure);
            }
        }
    }

    private void checkCancellation(SimulationCancellationToken cancellationToken) {
        if (cancellationToken.isCancellationRequested()) {
            throw new SimulationCancelledException();
        }
    }

    private static final class StagedFile {

        private final Path temporaryPath;
        private final Path targetPath;

        private boolean installed;

        private StagedFile(Path temporaryPath, Path targetPath) {
            this.temporaryPath = temporaryPath;
            this.targetPath = targetPath;
        }
    }
}
