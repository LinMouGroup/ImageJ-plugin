package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.DirectedParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import java.util.Random;

/** Brownian diffusion with a constant physical drift vector. */
public final class DirectedMotion implements MotionModel {

    private final DirectedParameters parameters;

    public DirectedMotion(DirectedParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Directed parameters cannot be null.");
        }
        this.parameters = parameters;
    }

    @Override
    public MotionType getType() {
        return MotionType.DIRECTED;
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
        double timeStep = context.getFrameIntervalSeconds();
        double stepSigma = Math.sqrt(
                2.0 * parameters.getDiffusionUm2PerSecond() * timeStep
        );
        double driftLength = parameters.getSpeedUmPerSecond() * timeStep;
        double driftX = driftLength * Math.cos(parameters.getDirectionRadians());
        double driftY = driftLength * Math.sin(parameters.getDirectionRadians());
        Random random = new Random(context.getRandomSeed());

        for (int frame = 1; frame < context.getFrames(); frame++) {
            x[frame] = x[frame - 1]
                    + driftX + stepSigma * random.nextGaussian();
            y[frame] = y[frame - 1]
                    + driftY + stepSigma * random.nextGaussian();
        }
        return new Trajectory(x, y);
    }

    private static void requireContext(MotionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Motion context cannot be null.");
        }
    }
}
