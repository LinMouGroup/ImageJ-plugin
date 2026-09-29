package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.AttmParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.CtrwParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.LevyWalkParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.SbmParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class AnomalousMotionInvariantTest {

    @Test
    public void ctrwContainsBothWaitingPlateausAndGaussianJumps() {
        double timeStep = 0.02;
        Trajectory trajectory = new CTRW(
                new CtrwParameters(0.70, 0.10, timeStep)
        ).generate(new MotionContext(5000, timeStep, 1414L));

        int plateaus = 0;
        int jumps = 0;
        for (int frame = 1; frame < trajectory.length(); frame++) {
            boolean unchanged = trajectory.getXUm(frame) == trajectory.getXUm(frame - 1)
                    && trajectory.getYUm(frame) == trajectory.getYUm(frame - 1);
            if (unchanged) {
                plateaus++;
            } else {
                jumps++;
            }
        }
        assertTrue(plateaus > 0);
        assertTrue(jumps > 0);
        assertTrue(plateaus > jumps);
    }

    @Test
    public void levyWalkHasFiniteSpeedAtEveryObservedStep() {
        double timeStep = 0.01;
        double speed = 1.30;
        Trajectory trajectory = new LevyWalk(
                new LevyWalkParameters(1.50, speed, timeStep)
        ).generate(new MotionContext(10000, timeStep, 1515L));

        double maximumStep = speed * timeStep + 1.0e-12;
        double totalNetDisplacement = 0.0;
        for (int frame = 1; frame < trajectory.length(); frame++) {
            double dx = trajectory.getXUm(frame) - trajectory.getXUm(frame - 1);
            double dy = trajectory.getYUm(frame) - trajectory.getYUm(frame - 1);
            double stepLength = Math.sqrt(dx * dx + dy * dy);
            assertTrue(stepLength <= maximumStep);
            totalNetDisplacement += stepLength;
        }
        assertTrue(totalNetDisplacement > 0.0);
    }

    @Test
    public void sbmEnsembleVarianceScalesAsTimeToAlpha() {
        double alpha = 0.70;
        int samples = 12000;
        double[] atTimeOne = new double[samples];
        double[] atTimeTwo = new double[samples];
        SBM model = new SBM(new SbmParameters(alpha, 0.05));
        Random seedSource = new Random(2024L);

        for (int sample = 0; sample < samples; sample++) {
            Trajectory trajectory = model.generate(
                    new MotionContext(3, 1.0, seedSource.nextLong())
            );
            atTimeOne[sample] = trajectory.getXUm(1);
            atTimeTwo[sample] = trajectory.getXUm(2);
        }

        double varianceRatio = variance(atTimeTwo) / variance(atTimeOne);
        assertTrue("SBM variance ratio was " + varianceRatio,
                Math.abs(varianceRatio - Math.pow(2.0, alpha)) < 0.08);
    }

    @Test
    public void attmIsFiniteNonTrivialAndSeedReproducible() {
        ATTM model = new ATTM(new AttmParameters(
                0.70,
                1.0,
                1.0 / 0.70,
                0.20,
                0.01
        ));
        MotionContext context = new MotionContext(2000, 0.01, 1616L);
        Trajectory first = model.generate(context);
        Trajectory second = model.generate(context);

        assertArrayEquals(first.copyXUm(), second.copyXUm(), 0.0);
        assertArrayEquals(first.copyYUm(), second.copyYUm(), 0.0);
        assertTrue(Math.abs(first.getXUm(first.length() - 1))
                + Math.abs(first.getYUm(first.length() - 1)) > 0.0);
    }

    private double variance(double[] values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        double mean = sum / values.length;
        double squaredSum = 0.0;
        for (double value : values) {
            double centered = value - mean;
            squaredSum += centered * centered;
        }
        return squaredSum / (values.length - 1);
    }
}
