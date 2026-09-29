package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Set;

/** Generates one normalized realized composition for a simulation run. */
public interface MotionCompositionGenerator {

    MotionCompositionConfig generate(
            Set<MotionType> selectedMotionTypes,
            RandomCompositionConfig randomCompositionConfig,
            long compositionSeed
    );
}
