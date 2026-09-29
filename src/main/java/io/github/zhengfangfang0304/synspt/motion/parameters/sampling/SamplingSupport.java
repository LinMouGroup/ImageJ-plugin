package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig.NumericRange;

import java.util.Random;

/** Shared distribution primitives for model-specific samplers. */
final class SamplingSupport {

    private SamplingSupport() {
    }

    static double uniform(Random random, NumericRange range) {
        return range.getMinimum()
                + random.nextDouble() * (range.getMaximum() - range.getMinimum());
    }

    static double logUniform(Random random, NumericRange range) {
        double logMinimum = Math.log(range.getMinimum());
        return Math.exp(logMinimum + random.nextDouble()
                * (Math.log(range.getMaximum()) - logMinimum));
    }

    static void validateInputs(
            Object distributionConfig,
            double frameIntervalSeconds,
            Random random
    ) {
        if (distributionConfig == null || random == null) {
            throw new IllegalArgumentException(
                    "Distribution config and random source cannot be null."
            );
        }
        if (Double.isNaN(frameIntervalSeconds)
                || Double.isInfinite(frameIntervalSeconds)
                || frameIntervalSeconds <= 0.0) {
            throw new IllegalArgumentException(
                    "Frame interval must be finite and positive."
            );
        }
    }
}
