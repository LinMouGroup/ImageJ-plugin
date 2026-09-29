package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import org.junit.Test;

import java.util.EnumSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RandomMotionCompositionGeneratorTest {

    @Test
    public void singleSelectionAlwaysReceivesTheWholeComposition() {
        Map<MotionType, Double> composition = generator().generate(
                EnumSet.of(MotionType.BROWNIAN),
                RandomCompositionConfig.defaultConfig(),
                1L
        ).getComposition();
        assertEquals(1, composition.size());
        assertEquals(1.0, composition.get(MotionType.BROWNIAN), 0.0);
    }

    @Test
    public void samplesOnlySelectedModelsAsStrictlyPositiveNormalizedRatios() {
        Map<MotionType, Double> composition = generator().generate(
                EnumSet.of(MotionType.BROWNIAN, MotionType.FBM, MotionType.CTRW),
                RandomCompositionConfig.defaultConfig(),
                12345L
        ).getComposition();
        double sum = 0.0;
        for (double ratio : composition.values()) {
            assertTrue(ratio > 0.0);
            sum += ratio;
        }
        assertEquals(1.0, sum, 1.0e-12);
        assertFalse(composition.containsKey(MotionType.DIRECTED));
    }

    @Test
    public void fixedSeedIsReproducibleAndDifferentSeedChangesComposition() {
        EnumSet<MotionType> selected = EnumSet.of(
                MotionType.BROWNIAN, MotionType.FBM, MotionType.CTRW
        );
        MotionCompositionConfig first = generator().generate(
                selected, RandomCompositionConfig.defaultConfig(), 99L
        );
        MotionCompositionConfig repeated = generator().generate(
                selected, RandomCompositionConfig.defaultConfig(), 99L
        );
        MotionCompositionConfig different = generator().generate(
                selected, RandomCompositionConfig.defaultConfig(), 100L
        );
        assertEquals(first.getComposition(), repeated.getComposition());
        assertFalse(first.getComposition().equals(different.getComposition()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptySelection() {
        generator().generate(
                EnumSet.noneOf(MotionType.class),
                RandomCompositionConfig.defaultConfig(),
                1L
        );
    }

    private RandomMotionCompositionGenerator generator() {
        return new RandomMotionCompositionGenerator();
    }
}
