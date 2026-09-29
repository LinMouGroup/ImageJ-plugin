package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.optical.PsfShapeType;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimulationConfigTest {

    @Test
    public void defaultConfigContainsOnlyBrownianMotion() {
        SimulationConfig config = SimulationConfig.defaultConfig();

        assertEquals(256, config.getImageWidth());
        assertEquals(256, config.getImageHeight());
        assertEquals(100, config.getFrames());
        assertEquals(100, config.getParticleNumber());
        assertEquals(EnumSet.of(MotionType.BROWNIAN), config.getSelectedMotionTypes());
        assertEquals(10.0, config.getNoiseConfig().getTargetSnr(), 0.0);
        assertEquals(PsfShapeType.CIRCULAR_GAUSSIAN, config.getSpotShape());
        assertEquals(8.0,
                config.getRandomCompositionConfig().getConcentration(), 0.0);
    }

    @Test
    public void selectedMotionTypesAreDefensivelyCopied() {
        EnumSet<MotionType> selected = EnumSet.of(
                MotionType.BROWNIAN,
                MotionType.FBM
        );
        SimulationConfig config = SimulationConfig.builder()
                .selectedMotionTypes(selected)
                .randomSeed(77L)
                .build();

        selected.clear();
        EnumSet<MotionType> returned = config.getSelectedMotionTypes();
        returned.clear();

        assertEquals(2, config.getSelectedMotionTypes().size());
        assertTrue(config.getSelectedMotionTypes().contains(MotionType.FBM));
        assertEquals(77L, config.getRandomSeed());
    }

    @Test
    public void builderStoresTheSimulationSpotShape() {
        SimulationConfig config = SimulationConfig.builder()
                .spotShape(PsfShapeType.ELLIPTICAL_GAUSSIAN)
                .build();

        assertEquals(PsfShapeType.ELLIPTICAL_GAUSSIAN,
                config.getSpotShape());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderRejectsNullSpotShape() {
        SimulationConfig.builder().spotShape(null);
    }
}
