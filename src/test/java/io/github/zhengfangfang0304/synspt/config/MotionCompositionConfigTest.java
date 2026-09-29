package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MotionCompositionConfigTest {

    @Test
    public void storesOnlyExplicitPositiveNormalizedRatios() {
        EnumMap<MotionType, Double> ratios =
                new EnumMap<MotionType, Double>(MotionType.class);
        ratios.put(MotionType.BROWNIAN, 0.6);
        ratios.put(MotionType.FBM, 0.3);
        ratios.put(MotionType.CTRW, 0.1);
        MotionCompositionConfig config = new MotionCompositionConfig(ratios);

        assertEquals(3, config.getComposition().size());
        assertEquals(0.6, config.getComposition().get(MotionType.BROWNIAN), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyComposition() {
        new MotionCompositionConfig(
                new EnumMap<MotionType, Double>(MotionType.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsZeroRatio() {
        EnumMap<MotionType, Double> ratios =
                new EnumMap<MotionType, Double>(MotionType.class);
        ratios.put(MotionType.BROWNIAN, 1.0);
        ratios.put(MotionType.FBM, 0.0);
        new MotionCompositionConfig(ratios);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsRatiosThatDoNotSumToOne() {
        EnumMap<MotionType, Double> ratios =
                new EnumMap<MotionType, Double>(MotionType.class);
        ratios.put(MotionType.BROWNIAN, 0.8);
        ratios.put(MotionType.FBM, 0.3);
        new MotionCompositionConfig(ratios);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnedCompositionCannotBeModified() {
        Map<MotionType, Double> ratios =
                new EnumMap<MotionType, Double>(MotionType.class);
        ratios.put(MotionType.BROWNIAN, 1.0);
        new MotionCompositionConfig(ratios).getComposition()
                .put(MotionType.FBM, 0.5);
    }
}
