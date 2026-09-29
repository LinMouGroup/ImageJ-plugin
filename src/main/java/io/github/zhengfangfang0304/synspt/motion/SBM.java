package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.SbmParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import java.util.Random;

/**
 * Scaled Brownian motion implemented as a deterministic time change of
 * Brownian motion.
 */
public final class SBM implements MotionModel {

    private final SbmParameters parameters;

    public SBM(SbmParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("SBM parameters cannot be null.");
        }
        this.parameters = parameters;
    }

    @Override
    public MotionType getType() {
        return MotionType.SBM;
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
        double alpha = parameters.getAlpha();
        double generalizedDiffusion =
                parameters.getGeneralizedDiffusionUm2PerSecondAlpha();
        double timeStep = context.getFrameIntervalSeconds();

        for (int frame = 1; frame < context.getFrames(); frame++) {
            double previousTime = (frame - 1) * timeStep;
            double currentTime = frame * timeStep;
            double operationalIncrement = Math.pow(currentTime, alpha)
                    - Math.pow(previousTime, alpha);
            double stepSigma = Math.sqrt(
                    2.0 * generalizedDiffusion * operationalIncrement
            );
            x[frame] = x[frame - 1] + stepSigma * random.nextGaussian();
            y[frame] = y[frame - 1] + stepSigma * random.nextGaussian();
        }
        return new Trajectory(x, y);
    }

    private static void requireContext(MotionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Motion context cannot be null.");
        }
    }
}
