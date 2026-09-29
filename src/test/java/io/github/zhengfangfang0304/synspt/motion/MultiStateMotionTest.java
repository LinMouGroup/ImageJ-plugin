package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.DirectedParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MultiStateMotionTest {

    @Test
    public void executesSegmentsContinuouslyWithoutAZeroStepAtTheBoundary() {
        StateSegment horizontal = new StateSegment(
                1,
                0,
                0,
                2,
                MotionType.DIRECTED,
                new DirectedParameters(0.0, 1.0, 0.0)
        );
        StateSegment vertical = new StateSegment(
                1,
                1,
                3,
                5,
                MotionType.DIRECTED,
                new DirectedParameters(0.0, 1.0, Math.PI / 2.0)
        );

        Trajectory trajectory = new MultiStateMotion(
                Arrays.asList(horizontal, vertical)
        ).generate(new MotionContext(6, 1.0, 18L));

        assertArrayEquals(
                new double[] {0.0, 1.0, 2.0, 2.0, 2.0, 2.0},
                trajectory.copyXUm(),
                1.0e-12
        );
        assertArrayEquals(
                new double[] {0.0, 0.0, 0.0, 1.0, 2.0, 3.0},
                trajectory.copyYUm(),
                1.0e-12
        );
        assertEquals(6, trajectory.length());
    }

    @Test
    public void fixedRootSeedReproducesACompositeTrajectory() {
        StateSegment first = new StateSegment(
                2, 0, 0, 4, MotionType.DIRECTED,
                new DirectedParameters(0.2, 1.0, 0.3)
        );
        StateSegment second = new StateSegment(
                2, 1, 5, 9, MotionType.DIRECTED,
                new DirectedParameters(0.4, 0.8, 1.7)
        );
        MultiStateMotion motion = new MultiStateMotion(
                Arrays.asList(first, second)
        );
        Trajectory left = motion.generate(new MotionContext(10, 0.02, 88L));
        Trajectory right = motion.generate(new MotionContext(10, 0.02, 88L));

        assertArrayEquals(left.copyXUm(), right.copyXUm(), 0.0);
        assertArrayEquals(left.copyYUm(), right.copyYUm(), 0.0);
    }
}
