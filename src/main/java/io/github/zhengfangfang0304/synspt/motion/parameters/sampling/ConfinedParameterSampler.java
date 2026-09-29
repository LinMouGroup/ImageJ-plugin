package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.ConfinedParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import java.util.Random;

/** Samples confined diffusion and compartment size. */
public final class ConfinedParameterSampler implements MotionParameterSampler {
    @Override public MotionType getMotionType() { return MotionType.CONFINED; }

    @Override
    public MotionParameters sample(MotionParameterDistributionConfig config,
            double frameIntervalSeconds, Random random) {
        SamplingSupport.validateInputs(config, frameIntervalSeconds, random);
        return new ConfinedParameters(
                SamplingSupport.logUniform(random, config.getConfinedDiffusion()),
                SamplingSupport.uniform(random, config.getConfinedRadius()),
                config.getConfinedSubsteps()
        );
    }
}
