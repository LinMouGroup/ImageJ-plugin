package io.github.zhengfangfang0304.synspt.motion;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MotionTypeTest {

    @Test
    public void exposesEightBasicModelsAndOneCompositeModel() {
        assertEquals(9, MotionType.values().length);
        assertEquals(8, MotionType.baseTypes().size());
        assertEquals(
                "Brownian Motion",
                MotionType.BROWNIAN.getDisplayName()
        );
        assertEquals(
                "Directed Motion",
                MotionType.DIRECTED.getDisplayName()
        );
        assertEquals(
                "Confined Diffusion",
                MotionType.CONFINED.getDisplayName()
        );
        assertEquals(
                "Fractional Brownian Motion",
                MotionType.FBM.getDisplayName()
        );
        assertEquals(
                "Continuous-Time Random Walk",
                MotionType.CTRW.getDisplayName()
        );
        assertEquals(
                "Lévy Walk",
                MotionType.LEVY_WALK.getDisplayName()
        );
        assertEquals(
                "Scaled Brownian Motion",
                MotionType.SBM.getDisplayName()
        );
        assertEquals(
                "Annealed Transient Time Motion",
                MotionType.ATTM.getDisplayName()
        );
        assertEquals(
                "Multi-state Motion",
                MotionType.MULTI_STATE.getDisplayName()
        );
    }
}
