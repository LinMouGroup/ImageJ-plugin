package io.github.zhengfangfang0304.synspt.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MotionParameterDistributionConfigTest {

    @Test
    public void defaultRangesCoverAllModelFamilies() {
        MotionParameterDistributionConfig config =
                MotionParameterDistributionConfig.defaultConfig();
        assertEquals(0.01, config.getBrownianDiffusion().getMinimum(), 0.0);
        assertTrue(config.getDirectedSpeed().getMaximum()
                > config.getDirectedSpeed().getMinimum());
        assertEquals(100, config.getConfinedSubsteps());
        assertTrue(config.getFbmHurst().getMaximum() < 1.0);
        assertTrue(config.getCtrwAlpha().getMinimum() > 0.0);
        assertTrue(config.getLevyWalkSigma().getMinimum() > 1.0);
        assertTrue(config.getLevyWalkSigma().getMaximum() < 2.0);
        assertTrue(config.getSbmAlpha().getMaximum() <= 2.0);
        assertTrue(config.getAttmGammaOffset().getMaximum() < 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void numericRangeRejectsReversedBounds() {
        new MotionParameterDistributionConfig.NumericRange(2.0, 1.0);
    }
}
