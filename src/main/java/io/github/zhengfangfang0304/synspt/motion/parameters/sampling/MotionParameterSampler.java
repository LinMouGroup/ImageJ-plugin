package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import java.util.Random;

/** Model-specific strategy for sampling one immutable parameter object. */
public interface MotionParameterSampler {

    MotionType getMotionType();

    MotionParameters sample(
            MotionParameterDistributionConfig distributionConfig,
            double frameIntervalSeconds,
            Random random
    );
}
