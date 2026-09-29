package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.DirectedParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import java.util.Random;

/** Samples diffusion, drift speed, and isotropic direction independently. */
public final class DirectedParameterSampler implements MotionParameterSampler {
    @Override public MotionType getMotionType() { return MotionType.DIRECTED; }

    @Override
    public MotionParameters sample(MotionParameterDistributionConfig config,
            double frameIntervalSeconds, Random random) {
        SamplingSupport.validateInputs(config, frameIntervalSeconds, random);
        return new DirectedParameters(
                SamplingSupport.logUniform(random, config.getDirectedDiffusion()),
                SamplingSupport.logUniform(random, config.getDirectedSpeed()),
                random.nextDouble() * 2.0 * Math.PI
        );
    }
}
