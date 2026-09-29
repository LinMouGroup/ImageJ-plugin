package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/** Dispatches parameter sampling to one dedicated strategy per motion type. */
public final class DefaultMotionParameterGenerator
        implements MotionParameterGenerator {

    private final Map<MotionType, MotionParameterSampler> samplers;

    public DefaultMotionParameterGenerator() {
        this(new MotionParameterSampler[] {
                new BrownianParameterSampler(),
                new DirectedParameterSampler(),
                new ConfinedParameterSampler(),
                new FbmParameterSampler(),
                new CtrwParameterSampler(),
                new LevyWalkParameterSampler(),
                new SbmParameterSampler(),
                new AttmParameterSampler()
        });
    }

    public DefaultMotionParameterGenerator(MotionParameterSampler[] samplers) {
        if (samplers == null) {
            throw new IllegalArgumentException("Motion samplers cannot be null.");
        }
        EnumMap<MotionType, MotionParameterSampler> mapped =
                new EnumMap<MotionType, MotionParameterSampler>(MotionType.class);
        for (MotionParameterSampler sampler : samplers) {
            if (sampler == null
                    || mapped.put(sampler.getMotionType(), sampler) != null) {
                throw new IllegalArgumentException(
                        "Motion samplers must be non-null and unique by type."
                );
            }
        }
        if (!mapped.keySet().equals(MotionType.baseTypes())) {
            throw new IllegalArgumentException(
                    "Every basic motion type must have exactly one parameter sampler."
            );
        }
        this.samplers = mapped;
    }

    @Override
    public MotionParameters generate(
            MotionType motionType,
            MotionParameterDistributionConfig distributionConfig,
            double frameIntervalSeconds,
            long parameterSeed
    ) {
        if (motionType == null || distributionConfig == null) {
            throw new IllegalArgumentException(
                    "Motion type and parameter distribution cannot be null."
            );
        }
        MotionParameterSampler sampler = samplers.get(motionType);
        if (sampler == null) {
            throw new IllegalArgumentException(
                    "Composite motion types do not have parameter samplers."
            );
        }
        return sampler.sample(
                distributionConfig,
                frameIntervalSeconds,
                new Random(parameterSeed)
        );
    }
}
