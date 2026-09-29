package io.github.zhengfangfang0304.synspt.scene;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReflectingBoundaryHandlerTest {

    @Test
    public void reflectsNegativeAndMultiplePositiveOvershoots() {
        ReflectingBoundaryHandler boundary = new ReflectingBoundaryHandler();

        assertEquals(2.0, boundary.apply(2.0, 10.0), 0.0);
        assertEquals(2.0, boundary.apply(-2.0, 10.0), 0.0);
        assertEquals(8.0, boundary.apply(12.0, 10.0), 0.0);
        assertEquals(2.0, boundary.apply(22.0, 10.0), 0.0);
        assertEquals(8.0, boundary.apply(-12.0, 10.0), 0.0);
    }

    @Test
    public void exactUpperBoundaryIsMappedInsideHalfOpenField() {
        double mapped = new ReflectingBoundaryHandler().apply(10.0, 10.0);
        assertTrue(mapped >= 0.0);
        assertTrue(mapped < 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonFiniteCoordinates() {
        new ReflectingBoundaryHandler().apply(Double.NaN, 10.0);
    }
}
