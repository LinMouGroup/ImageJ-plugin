package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;

/**
 * Top-level trajectory and scene simulation use-case contract.
 */
public interface SimulationEngine {

    default SimulationResult generate(
            SimulationConfig config,
            SimulationProgressListener progressListener
    ) {
        return generate(
                config,
                progressListener,
                SimulationCancellationToken.NONE
        );
    }

    SimulationResult generate(
            SimulationConfig config,
            SimulationProgressListener progressListener,
            SimulationCancellationToken cancellationToken
    );
}
