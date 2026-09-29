package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.sampling.DefaultMotionParameterGenerator;
import io.github.zhengfangfang0304.synspt.motion.parameters.sampling.MotionParameterGenerator;
import io.github.zhengfangfang0304.synspt.util.SeedDerivation;

/** Samples independent immutable parameters for one multi-state segment. */
public final class StateParameterGenerator {

    private final MotionParameterGenerator parameterGenerator;

    public StateParameterGenerator() {
        this(new DefaultMotionParameterGenerator());
    }

    public StateParameterGenerator(MotionParameterGenerator parameterGenerator) {
        if (parameterGenerator == null) {
            throw new IllegalArgumentException(
                    "Motion parameter generator cannot be null."
            );
        }
        this.parameterGenerator = parameterGenerator;
    }

    public MotionParameters generate(
            MotionType motionType,
            MotionParameterDistributionConfig distributionConfig,
            double frameIntervalSeconds,
            long rootSeed,
            int particleId,
            int stateIndex
    ) {
        if (motionType == null || motionType.isComposite()) {
            throw new IllegalArgumentException(
                    "A state requires one basic motion type."
            );
        }
        return parameterGenerator.generate(
                motionType,
                distributionConfig,
                frameIntervalSeconds,
                SeedDerivation.multiStateParameterSeed(
                        rootSeed,
                        particleId,
                        stateIndex
                )
        );
    }
}
