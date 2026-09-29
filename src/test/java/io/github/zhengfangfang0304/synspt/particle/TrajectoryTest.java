package io.github.zhengfangfang0304.synspt.particle;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TrajectoryTest {

    @Test
    public void trajectoryDefensivelyCopiesBothAxes() {
        double[] x = new double[] {0.0, 1.0, 2.0};
        double[] y = new double[] {0.0, -1.0, -2.0};
        Trajectory trajectory = new Trajectory(x, y);

        x[1] = 99.0;
        y[1] = 99.0;
        double[] returnedX = trajectory.copyXUm();
        returnedX[2] = 99.0;

        assertEquals(3, trajectory.length());
        assertEquals(1.0, trajectory.getXUm(1), 0.0);
        assertEquals(-1.0, trajectory.getYUm(1), 0.0);
        assertArrayEquals(new double[] {0.0, 1.0, 2.0}, trajectory.copyXUm(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonFiniteCoordinates() {
        new Trajectory(
                new double[] {0.0, Double.NaN},
                new double[] {0.0, 1.0}
        );
    }
}
