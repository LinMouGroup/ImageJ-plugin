package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.LevyWalkParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import java.util.Random;

/** Samples Lévy exponent, finite speed, and flight-time cutoff. */
public final class LevyWalkParameterSampler implements MotionParameterSampler {
    @Override public MotionType getMotionType() { return MotionType.LEVY_WALK; }

    @Override
    public MotionParameters sample(MotionParameterDistributionConfig config,
            double frameIntervalSeconds, Random random) {
        SamplingSupport.validateInputs(config, frameIntervalSeconds, random);
        return new LevyWalkParameters(
                SamplingSupport.uniform(random, config.getLevyWalkSigma()),
                SamplingSupport.logUniform(random, config.getLevyWalkSpeed()),
                frameIntervalSeconds * SamplingSupport.logUniform(
                        random, config.getLevyWalkFlightTimeFactor())
        );
    }
}
