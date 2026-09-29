package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.BrownianParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.ConfinedParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.DirectedParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassicalMotionStatisticsTest {

    @Test
    public void brownianIncrementVarianceMatchesTwoDDeltaT() {
        double diffusion = 0.30;
        double timeStep = 0.02;
        Trajectory trajectory = new BrownianMotion(
                new BrownianParameters(diffusion)
        ).generate(new MotionContext(50001, timeStep, 1234L));
        double[] increments = xIncrements(trajectory);

        assertEquals(0.0, mean(increments), 0.0015);
        assertEquals(2.0 * diffusion * timeStep, variance(increments), 0.0008);
    }

    @Test
    public void directedMeanIncrementMatchesDriftVector() {
        double diffusion = 0.02;
        double speed = 0.80;
        double timeStep = 0.02;
        Trajectory trajectory = new DirectedMotion(
                new DirectedParameters(diffusion, speed, 0.0)
        ).generate(new MotionContext(50001, timeStep, 5678L));

        assertEquals(speed * timeStep, mean(xIncrements(trajectory)), 0.0007);
        assertEquals(0.0, mean(yIncrements(trajectory)), 0.0007);
    }

    @Test
    public void confinedTrajectoryNeverLeavesItsCircularCompartment() {
        double radius = 0.35;
        Trajectory trajectory = new ConfinedDiffusion(
                new ConfinedParameters(0.40, radius, 100)
        ).generate(new MotionContext(5000, 0.03, 909L));

        double radiusSquared = radius * radius;
        for (int frame = 0; frame < trajectory.length(); frame++) {
            double distanceSquared = trajectory.getXUm(frame) * trajectory.getXUm(frame)
                    + trajectory.getYUm(frame) * trajectory.getYUm(frame);
            assertTrue(distanceSquared <= radiusSquared + 1.0e-14);
        }
    }

    private double[] xIncrements(Trajectory trajectory) {
        double[] increments = new double[trajectory.length() - 1];
        for (int index = 0; index < increments.length; index++) {
            increments[index] = trajectory.getXUm(index + 1) - trajectory.getXUm(index);
        }
        return increments;
    }

    private double[] yIncrements(Trajectory trajectory) {
        double[] increments = new double[trajectory.length() - 1];
        for (int index = 0; index < increments.length; index++) {
            increments[index] = trajectory.getYUm(index + 1) - trajectory.getYUm(index);
        }
        return increments;
    }

    private double mean(double[] values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private double variance(double[] values) {
        double mean = mean(values);
        double squaredSum = 0.0;
        for (double value : values) {
            double centered = value - mean;
            squaredSum += centered * centered;
        }
        return squaredSum / (values.length - 1);
    }
}
