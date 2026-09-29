package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.AttmParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import java.util.Random;

/** Samples ATTM parameters while enforcing sigma &lt; gamma &lt; sigma + 1. */
public final class AttmParameterSampler implements MotionParameterSampler {
    @Override public MotionType getMotionType() { return MotionType.ATTM; }

    @Override
    public MotionParameters sample(MotionParameterDistributionConfig config,
            double frameIntervalSeconds, Random random) {
        SamplingSupport.validateInputs(config, frameIntervalSeconds, random);
        double sigma = SamplingSupport.uniform(random, config.getAttmSigma());
        double gamma = sigma + SamplingSupport.uniform(
                random, config.getAttmGammaOffset()
        );
        double alpha = sigma / gamma;
        return new AttmParameters(
                alpha,
                sigma,
                gamma,
                SamplingSupport.logUniform(random,
                        config.getAttmMaximumDiffusion()),
                frameIntervalSeconds * SamplingSupport.logUniform(
                        random, config.getAttmDwellTimeFactor())
        );
    }
}
