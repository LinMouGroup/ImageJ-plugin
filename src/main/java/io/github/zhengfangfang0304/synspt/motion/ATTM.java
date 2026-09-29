package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.AttmParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import java.util.Random;

/**
 * Annealed transient-time motion with a shared time-varying diffusivity for
 * both spatial axes.
 */
public final class ATTM implements MotionModel {

    private static final double TIME_EPSILON = 1.0e-15;

    private final AttmParameters parameters;

    public ATTM(AttmParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("ATTM parameters cannot be null.");
        }
        this.parameters = parameters;
    }

    @Override
    public MotionType getType() {
        return MotionType.ATTM;
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
        DiffusivityState state = drawState(random);

        for (int frame = 1; frame < context.getFrames(); frame++) {
            double remainingFrameTime = context.getFrameIntervalSeconds();
            double stepX = 0.0;
            double stepY = 0.0;

            while (remainingFrameTime > TIME_EPSILON) {
                double segmentDuration = Math.min(
                        remainingFrameTime,
                        state.remainingTimeSeconds
                );
                double segmentSigma = Math.sqrt(
                        2.0 * state.diffusionUm2PerSecond * segmentDuration
                );
                stepX += segmentSigma * random.nextGaussian();
                stepY += segmentSigma * random.nextGaussian();
                remainingFrameTime -= segmentDuration;
                state.remainingTimeSeconds -= segmentDuration;

                if (state.remainingTimeSeconds <= TIME_EPSILON) {
                    state = drawState(random);
                }
            }
            x[frame] = x[frame - 1] + stepX;
            y[frame] = y[frame - 1] + stepY;
        }
        return new Trajectory(x, y);
    }

    private DiffusivityState drawState(Random random) {
        double uniformOpenClosed = 1.0 - random.nextDouble();
        double normalizedDiffusion = Math.pow(
                uniformOpenClosed,
                1.0 / parameters.getDiffusivityExponentSigma()
        );
        double diffusion = parameters.getMaximumDiffusionUm2PerSecond()
                * normalizedDiffusion;
        double dwellTime = parameters.getMinimumDwellTimeSeconds()
                * Math.pow(
                normalizedDiffusion,
                -parameters.getDwellExponentGamma()
        );
        return new DiffusivityState(diffusion, dwellTime);
    }

    private static void requireContext(MotionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Motion context cannot be null.");
        }
    }

    private static final class DiffusivityState {

        private final double diffusionUm2PerSecond;
        private double remainingTimeSeconds;

        private DiffusivityState(
                double diffusionUm2PerSecond,
                double remainingTimeSeconds
        ) {
            this.diffusionUm2PerSecond = diffusionUm2PerSecond;
            this.remainingTimeSeconds = remainingTimeSeconds;
        }
    }
}
