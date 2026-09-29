package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.BrownianParameters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StateSegmentTest {

    @Test
    public void storesAnInclusiveBasicMotionInterval() {
        BrownianParameters parameters = new BrownianParameters(0.2);
        StateSegment segment = new StateSegment(
                3, 1, 10, 24, MotionType.BROWNIAN, parameters
        );

        assertEquals(3, segment.getParticleId());
        assertEquals(1, segment.getStateIndex());
        assertEquals(15, segment.getDurationFrames());
        assertTrue(segment.containsFrame(10));
        assertTrue(segment.containsFrame(24));
        assertFalse(segment.containsFrame(25));
        assertEquals(parameters, segment.getMotionParameters());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAParameterTypeMismatch() {
        new StateSegment(
                1,
                0,
                0,
                9,
                MotionType.DIRECTED,
                new BrownianParameters(0.2)
        );
    }
}
