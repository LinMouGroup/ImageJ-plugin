package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.CtrwParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import java.util.Random;

/** Samples subdiffusive CTRW exponent, jump scale, and waiting cutoff. */
public final class CtrwParameterSampler implements MotionParameterSampler {
    @Override public MotionType getMotionType() { return MotionType.CTRW; }

    @Override
    public MotionParameters sample(MotionParameterDistributionConfig config,
            double frameIntervalSeconds, Random random) {
        SamplingSupport.validateInputs(config, frameIntervalSeconds, random);
        return new CtrwParameters(
                SamplingSupport.uniform(random, config.getCtrwAlpha()),
                SamplingSupport.logUniform(random, config.getCtrwDiffusion()),
                frameIntervalSeconds * SamplingSupport.logUniform(
                        random, config.getCtrwWaitingTimeFactor())
        );
    }
}
