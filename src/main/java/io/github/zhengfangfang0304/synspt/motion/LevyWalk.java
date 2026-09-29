package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.LevyWalkParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import java.util.Random;

/**
 * Isotropic two-dimensional Lévy walk with finite constant speed.
 *
 * <p>Flight duration and flight length are coupled by length = speed × time.
 * A single flight clock and direction are shared by both axes.</p>
 */
public final class LevyWalk implements MotionModel {

    private static final double TWO_PI = 2.0 * Math.PI;

    private final LevyWalkParameters parameters;

    public LevyWalk(LevyWalkParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Lévy walk parameters cannot be null.");
        }
        this.parameters = parameters;
    }

    @Override
    public MotionType getType() {
        return MotionType.LEVY_WALK;
    }

    @Override
    public MotionParameters getParameters() {
        return parameters;
    }

    @Override
    public Trajectory generate(MotionContext context) {
        requireContext(context);
        double[] x = new double[context.getFrames()];
        double[] y = new double[context.getFrames()];
        Random random = new Random(context.getRandomSeed());

        double currentTime = 0.0;
        double currentX = 0.0;
        double currentY = 0.0;
        double direction = random.nextDouble() * TWO_PI;
        double flightEndTime = drawFlightTime(random);

        for (int frame = 1; frame < context.getFrames(); frame++) {
            double targetTime = frame * context.getFrameIntervalSeconds();
            while (currentTime < targetTime) {
                double segmentEnd = Math.min(targetTime, flightEndTime);
                double segmentDuration = segmentEnd - currentTime;
                double distance = parameters.getSpeedUmPerSecond() * segmentDuration;
                currentX += distance * Math.cos(direction);
                currentY += distance * Math.sin(direction);
                currentTime = segmentEnd;

                if (currentTime >= flightEndTime) {
                    direction = random.nextDouble() * TWO_PI;
                    double duration = drawFlightTime(random);
                    flightEndTime = Double.isInfinite(duration)
                            ? Double.POSITIVE_INFINITY
                            : currentTime + duration;
                }
            }
            x[frame] = currentX;
            y[frame] = currentY;
        }
        return new Trajectory(x, y);
    }

    private double drawFlightTime(Random random) {
        double uniformOpenClosed = 1.0 - random.nextDouble();
        return parameters.getMinimumFlightTimeSeconds()
                * Math.pow(
                uniformOpenClosed,
                -1.0 / parameters.getFlightTimeExponentSigma()
        );
    }

    private static void requireContext(MotionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Motion context cannot be null.");
        }
    }
}
