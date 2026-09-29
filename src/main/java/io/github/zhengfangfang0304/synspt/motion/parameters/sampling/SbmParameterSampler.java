package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.SbmParameters;

import java.util.Random;

/** Samples SBM anomalous exponent and generalized diffusion. */
public final class SbmParameterSampler implements MotionParameterSampler {
    @Override public MotionType getMotionType() { return MotionType.SBM; }

    @Override
    public MotionParameters sample(MotionParameterDistributionConfig config,
            double frameIntervalSeconds, Random random) {
        SamplingSupport.validateInputs(config, frameIntervalSeconds, random);
        return new SbmParameters(
                SamplingSupport.uniform(random, config.getSbmAlpha()),
                SamplingSupport.logUniform(random,
                        config.getSbmGeneralizedDiffusion())
        );
    }
}
