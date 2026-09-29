package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import org.junit.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MotionWeightRegistryTest {

    @Test
    public void containsEveryMotionType() {
        Map<MotionType, Double> weights =
                new MotionWeightRegistry().getDefaultWeights();

        assertEquals(MotionType.values().length, weights.size());
        for (MotionType motionType : MotionType.values()) {
            assertTrue(weights.containsKey(motionType));
        }
    }

    @Test
    public void everyDefaultWeightIsPositive() {
        MotionWeightRegistry registry = new MotionWeightRegistry();

        for (MotionType motionType : MotionType.values()) {
            assertTrue(registry.getWeight(motionType) > 0.0);
        }
    }

    @Test
    public void multiStateHasAnInternalDefaultCompositionWeight() {
        assertEquals(
                10.0,
                new MotionWeightRegistry().getWeight(MotionType.MULTI_STATE),
                0.0
        );
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnedWeightMapCannotBeModified() {
        new MotionWeightRegistry().getDefaultWeights().put(
                MotionType.BROWNIAN,
                1.0
        );
    }

    @Test
    public void alternativeDefaultWeightChangesTheDirichletPrior() {
        MotionWeightRegistry defaults = new MotionWeightRegistry();
        EnumMap<MotionType, Double> modifiedWeights =
                new EnumMap<MotionType, Double>(defaults.getDefaultWeights());
        modifiedWeights.put(MotionType.BROWNIAN, 80.0);
        MotionWeightRegistry modifiedRegistry =
                new MotionWeightRegistry(modifiedWeights);

        RandomCompositionConfig concentratedPrior =
                new RandomCompositionConfig(1000000.0);
        Map<MotionType, Double> defaultComposition =
                new RandomMotionCompositionGenerator(
                        defaults, new DirichletSampler()
                ).generate(
                        EnumSet.of(MotionType.BROWNIAN, MotionType.FBM),
                        concentratedPrior,
                        123L
                ).getComposition();
        Map<MotionType, Double> modifiedComposition =
                new RandomMotionCompositionGenerator(
                        modifiedRegistry, new DirichletSampler()
                ).generate(
                        EnumSet.of(MotionType.BROWNIAN, MotionType.FBM),
                        concentratedPrior,
                        123L
                ).getComposition();

        assertTrue(modifiedComposition.get(MotionType.BROWNIAN)
                > defaultComposition.get(MotionType.BROWNIAN));
    }
}
