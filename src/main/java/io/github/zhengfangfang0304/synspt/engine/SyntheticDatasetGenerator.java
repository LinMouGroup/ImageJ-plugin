package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.image.ImagePlusGenerator;

/** End-to-end trajectory, optical, noise, and ImagePlus orchestration. */
public final class SyntheticDatasetGenerator {

    private static final int TRAJECTORY_PROGRESS_RANGE = 35;
    private static final int IMAGE_PROGRESS_OFFSET = 35;
    private static final int IMAGE_PROGRESS_RANGE = 65;

    private final SimulationEngine simulationEngine;
    private final ImagePlusGenerator imagePlusGenerator;

    public SyntheticDatasetGenerator() {
        this(new DefaultSimulationEngine(), new ImagePlusGenerator());
    }

    public SyntheticDatasetGenerator(
            SimulationEngine simulationEngine,
            ImagePlusGenerator imagePlusGenerator
    ) {
        if (simulationEngine == null || imagePlusGenerator == null) {
            throw new IllegalArgumentException(
                    "Simulation engine and ImagePlus generator cannot be null."
            );
        }
        this.simulationEngine = simulationEngine;
        this.imagePlusGenerator = imagePlusGenerator;
    }

    public RenderedSimulationResult generate(
            SimulationConfig config,
            SimulationProgressListener progressListener
    ) {
        return generate(
                config,
                progressListener,
                SimulationCancellationToken.NONE
        );
    }

    public RenderedSimulationResult generate(
            SimulationConfig config,
            SimulationProgressListener progressListener,
            SimulationCancellationToken cancellationToken
    ) {
        if (progressListener == null || cancellationToken == null) {
            throw new IllegalArgumentException(
                    "Progress listener and cancellation token cannot be null."
            );
        }
        SimulationResult simulationResult = simulationEngine.generate(
                config,
                scaled(progressListener, 0, TRAJECTORY_PROGRESS_RANGE),
                cancellationToken
        );
        return imagePlusGenerator.generate(
                simulationResult,
                scaled(
                        progressListener,
                        IMAGE_PROGRESS_OFFSET,
                        IMAGE_PROGRESS_RANGE
                ),
                cancellationToken
        );
    }

    private SimulationProgressListener scaled(
            final SimulationProgressListener delegate,
            final int offset,
            final int range
    ) {
        return new SimulationProgressListener() {
            @Override
            public void onProgress(int percent, String message) {
                int boundedPercent = Math.max(0, Math.min(100, percent));
                delegate.onProgress(
                        offset + range * boundedPercent / 100,
                        message
                );
            }
        };
    }
}
