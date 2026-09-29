package io.github.zhengfangfang0304.particletracking.simulation.motion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MotionProfileResolverTest {

    private static final double TOLERANCE = 1.0e-12;

    private final MotionProfileResolver resolver =
            new MotionProfileResolver();

    @Test
    void normalDiffusionUsesHurstOneHalf() {
        MotionProfile profile =
                resolver.resolve(MotionType.NORMAL_DIFFUSION);

        assertEquals(0.5, profile.getHurstExponent(), TOLERANCE);
        assertTrue(profile.getDriftModel().isNone());
    }

    @Test
    void subdiffusionUsesHurstBelowOneHalf() {
        MotionProfile profile =
                resolver.resolve(MotionType.SUBDIFFUSION);
        double hurst = profile.getHurstExponent();

        assertEquals(0.3, hurst, TOLERANCE);
        assertTrue(hurst > 0.0 && hurst < 0.5);
    }

    @Test
    void superdiffusionUsesHurstAboveOneHalf() {
        MotionProfile profile =
                resolver.resolve(MotionType.SUPERDIFFUSION);
        double hurst = profile.getHurstExponent();

        assertEquals(0.75, hurst, TOLERANCE);
        assertTrue(hurst > 0.5 && hurst < 1.0);
    }

    @Test
    void directedAnomalousDiffusionHasDriftAndAnomalousHurst() {
        MotionProfile profile =
                resolver.resolve(
                        MotionType.DIRECTED_ANOMALOUS_DIFFUSION
                );
        DriftModel drift = profile.getDriftModel();

        assertNotEquals(
                0.5,
                profile.getHurstExponent(),
                TOLERANCE
        );
        assertFalse(drift.isNone());
        assertEquals(
                DriftModel.Type.CONSTANT_DRIFT,
                drift.getType()
        );
        assertTrue(
                drift.getVelocityXUmPerSecond() != 0.0
                        || drift.getVelocityYUmPerSecond() != 0.0
        );
    }

    @Test
    void immobileUsesZeroDiffusionAndNoDrift() {
        MotionProfile profile =
                resolver.resolve(MotionType.IMMOBILE);

        assertEquals(
                MotionType.IMMOBILE,
                profile.getMotionType()
        );
        assertEquals(0.5, profile.getHurstExponent(), TOLERANCE);
        assertEquals(
                0.0,
                profile.getGeneralizedDiffusionCoefficient(),
                TOLERANCE
        );
        assertTrue(profile.getDriftModel().isNone());
        assertEquals(1.0, profile.getAlpha(), TOLERANCE);
    }

    @Test
    void alphaIsAlwaysTwiceTheHurstExponent() {
        for (MotionType motionType : MotionType.values()) {
            MotionProfile profile = resolver.resolve(motionType);

            assertEquals(
                    2.0 * profile.getHurstExponent(),
                    profile.getAlpha(),
                    TOLERANCE
            );
        }
    }
}
