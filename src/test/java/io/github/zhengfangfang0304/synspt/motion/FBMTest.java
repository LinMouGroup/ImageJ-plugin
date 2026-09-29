package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.FbmParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FBMTest {

    @Test
    public void createsAReproduciblePhysicalPathBeginningAtZero() {
        FBM model = new FBM(new FbmParameters(0.35, 0.05));
        MotionContext context = new MotionContext(1024, 1.0 / 30.0, 1717L);

        Trajectory first = model.generate(context);
        Trajectory second = model.generate(context);

        assertEquals(1024, first.length());
        assertEquals(0.0, first.getXUm(0), 0.0);
        assertEquals(0.0, first.getYUm(0), 0.0);
        assertArrayEquals(first.copyXUm(), second.copyXUm(), 0.0);
        assertArrayEquals(first.copyYUm(), second.copyYUm(), 0.0);
    }
}
