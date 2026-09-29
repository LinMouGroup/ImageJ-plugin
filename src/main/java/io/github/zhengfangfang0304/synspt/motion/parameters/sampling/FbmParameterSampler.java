package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.FbmParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import java.util.Random;

/** Samples FBM Hurst and generalized-diffusion parameters independently. */
public final class FbmParameterSampler implements MotionParameterSampler {
    @Override public MotionType getMotionType() { return MotionType.FBM; }

    @Override
    public MotionParameters sample(MotionParameterDistributionConfig config,
            double frameIntervalSeconds, Random random) {
        SamplingSupport.validateInputs(config, frameIntervalSeconds, random);
        return new FbmParameters(
                SamplingSupport.uniform(random, config.getFbmHurst()),
                SamplingSupport.logUniform(random,
                        config.getFbmGeneralizedDiffusion())
        );
    }
}
