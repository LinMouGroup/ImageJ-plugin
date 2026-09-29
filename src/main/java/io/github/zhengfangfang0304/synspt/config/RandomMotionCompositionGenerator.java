package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Samples selected-model ratios from a weighted Dirichlet prior. */
public final class RandomMotionCompositionGenerator
        implements MotionCompositionGenerator {

    private final MotionWeightRegistry weightRegistry;
    private final DirichletSampler dirichletSampler;

    public RandomMotionCompositionGenerator() {
        this(new MotionWeightRegistry(), new DirichletSampler());
    }

    public RandomMotionCompositionGenerator(
            MotionWeightRegistry weightRegistry,
            DirichletSampler dirichletSampler
    ) {
        if (weightRegistry == null || dirichletSampler == null) {
            throw new IllegalArgumentException(
                    "Weight registry and Dirichlet sampler cannot be null."
            );
        }
        this.weightRegistry = weightRegistry;
        this.dirichletSampler = dirichletSampler;
    }

    @Override
    public MotionCompositionConfig generate(
            Set<MotionType> selectedMotionTypes,
            RandomCompositionConfig randomCompositionConfig,
            long compositionSeed
    ) {
        if (selectedMotionTypes == null || selectedMotionTypes.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one motion type must be selected."
            );
        }
        if (randomCompositionConfig == null) {
            throw new IllegalArgumentException(
                    "Random composition config cannot be null."
            );
        }
        EnumMap<MotionType, Double> selectedWeights =
                new EnumMap<MotionType, Double>(MotionType.class);
        double weightSum = 0.0;
        for (MotionType type : selectedMotionTypes) {
            if (type == null) {
                throw new IllegalArgumentException(
                        "Selected motion types cannot contain null."
                );
            }
            double weight = weightRegistry.getWeight(type);
            selectedWeights.put(type, weight);
            weightSum += weight;
        }
        if (selectedWeights.size() == 1) {
            Map.Entry<MotionType, Double> entry =
                    selectedWeights.entrySet().iterator().next();
            entry.setValue(1.0);
            return new MotionCompositionConfig(selectedWeights);
        }

        double[] alpha = new double[selectedWeights.size()];
        int index = 0;
        for (double weight : selectedWeights.values()) {
            alpha[index++] = randomCompositionConfig.getConcentration()
                    * weight / weightSum;
        }
        double[] ratios = dirichletSampler.sample(
                alpha,
                new Random(compositionSeed)
        );
        EnumMap<MotionType, Double> composition =
                new EnumMap<MotionType, Double>(MotionType.class);
        index = 0;
        for (MotionType type : selectedWeights.keySet()) {
            composition.put(type, ratios[index++]);
        }
        return new MotionCompositionConfig(composition);
    }
}
