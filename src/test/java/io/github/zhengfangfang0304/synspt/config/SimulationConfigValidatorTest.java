package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import org.junit.Test;

import java.util.EnumSet;

public class SimulationConfigValidatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAnEmptyModelSelection() {
        SimulationConfig.builder()
                .selectedMotionTypes(EnumSet.noneOf(MotionType.class))
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsFewerParticlesThanSelectedModels() {
        SimulationConfig.builder()
                .particleNumber(1)
                .selectedMotionTypes(EnumSet.of(
                        MotionType.BROWNIAN,
                        MotionType.FBM
                ))
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveDimensions() {
        SimulationConfig.builder()
                .imageWidth(0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAnOverflowingPhysicalFieldOfView() {
        MicroscopeConfig microscopeConfig = new MicroscopeConfig(
                Double.MAX_VALUE,
                MicroscopeConfig.DEFAULT_FRAME_INTERVAL_SECONDS,
                MicroscopeConfig.DEFAULT_BACKGROUND_PHOTONS,
                MicroscopeConfig.DEFAULT_READ_NOISE_SIGMA_ADU,
                MicroscopeConfig.DEFAULT_GAIN_ADU_PER_PHOTON,
                MicroscopeConfig.DEFAULT_OFFSET_ADU,
                MicroscopeConfig.DEFAULT_BIT_DEPTH
        );
        SimulationConfig.builder()
                .imageWidth(2)
                .microscopeConfig(microscopeConfig)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMultiStateMotionWithOnlyOneFrame() {
        SimulationConfig.builder()
                .frames(1)
                .particleNumber(1)
                .selectedMotionTypes(EnumSet.of(MotionType.MULTI_STATE))
                .build();
    }
}
