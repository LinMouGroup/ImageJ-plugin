package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.noise.SnrDefinition;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NoiseConfigTest {

    @Test
    public void defaultTargetIsTenWithExplicitDefinition() {
        NoiseConfig config = NoiseConfig.defaultConfig();
        assertEquals(10.0, config.getTargetSnr(), 0.0);
        assertEquals(
                SnrDefinition.PEAK_PIXEL_SIGNAL,
                config.getSnrDefinition()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveTarget() {
        new NoiseConfig(0.0, SnrDefinition.PEAK_PIXEL_SIGNAL);
    }
}
