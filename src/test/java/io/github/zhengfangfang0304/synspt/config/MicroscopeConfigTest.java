package io.github.zhengfangfang0304.synspt.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MicroscopeConfigTest {

    @Test
    public void defaultsMatchTheApprovedHiddenImagingProfile() {
        MicroscopeConfig config = MicroscopeConfig.defaultConfig();

        assertEquals(0.1, config.getPixelSizeUm(), 0.0);
        assertEquals(1.0 / 30.0, config.getFrameIntervalSeconds(), 0.0);
        assertEquals(20.0, config.getBackgroundPhotons(), 0.0);
        assertEquals(2.0, config.getReadNoiseSigmaAdu(), 0.0);
        assertEquals(1.0, config.getGainAduPerPhoton(), 0.0);
        assertEquals(100.0, config.getOffsetAdu(), 0.0);
        assertEquals(16, config.getBitDepth());
    }
}
