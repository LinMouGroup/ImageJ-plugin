package io.github.zhengfangfang0304.synspt.motion.parameters;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MotionParametersTest {

    @Test
    public void derivedAnomalousExponentsFollowModelDefinitions() {
        assertEquals(0.70, new FbmParameters(0.35, 0.05).getAlpha(), 1.0e-12);
        assertEquals(
                1.50,
                new LevyWalkParameters(1.50, 1.0, 0.01).getAlpha(),
                1.0e-12
        );
        assertEquals(
                0.70,
                new AttmParameters(
                        0.70,
                        1.0,
                        1.0 / 0.70,
                        0.20,
                        0.01
                ).getAlpha(),
                1.0e-12
        );
    }

    @Test(expected = UnsupportedOperationException.class)
    public void metadataIsImmutableAndOrderedByConstruction() {
        Map<String, String> metadata = new DirectedParameters(
                0.05,
                1.0,
                0.0
        ).toMetadata();
        assertEquals("D_um2_per_s", metadata.keySet().iterator().next());
        metadata.put("illegal", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInconsistentAttmExponents() {
        new AttmParameters(0.8, 1.0, 1.0 / 0.7, 0.2, 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBallisticOrInvalidFbmHurst() {
        new FbmParameters(1.0, 0.05);
    }
}
