package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.BrownianParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import java.util.Random;

/** Samples free-diffusion coefficients log-uniformly. */
public final class BrownianParameterSampler implements MotionParameterSampler {
    @Override public MotionType getMotionType() { return MotionType.BROWNIAN; }

    @Override
    public MotionParameters sample(MotionParameterDistributionConfig config,
            double frameIntervalSeconds, Random random) {
        SamplingSupport.validateInputs(config, frameIntervalSeconds, random);
        return new BrownianParameters(
                SamplingSupport.logUniform(random, config.getBrownianDiffusion())
        );
    }
}
