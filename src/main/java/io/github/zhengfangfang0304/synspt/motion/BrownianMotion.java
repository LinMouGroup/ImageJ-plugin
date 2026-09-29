package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.BrownianParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import java.util.Random;

/** Two-dimensional free Brownian diffusion. */
public final class BrownianMotion implements MotionModel {

    private final BrownianParameters parameters;

    public BrownianMotion(BrownianParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Brownian parameters cannot be null.");
        }
        this.parameters = parameters;
    }

    @Override
    public MotionType getType() {
        return MotionType.BROWNIAN;
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
        double stepSigma = Math.sqrt(
                2.0
                        * parameters.getDiffusionUm2PerSecond()
                        * context.getFrameIntervalSeconds()
        );
        Random random = new Random(context.getRandomSeed());

        for (int frame = 1; frame < context.getFrames(); frame++) {
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
