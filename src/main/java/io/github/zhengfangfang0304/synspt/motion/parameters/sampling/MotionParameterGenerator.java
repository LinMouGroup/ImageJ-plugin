package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

/** Generates typed parameters without generating trajectory randomness. */
public interface MotionParameterGenerator {

    MotionParameters generate(
            MotionType motionType,
            MotionParameterDistributionConfig distributionConfig,
            double frameIntervalSeconds,
            long parameterSeed
    );
}
