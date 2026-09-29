package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.ConfinedParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import java.util.Random;

/**
 * Brownian diffusion inside a circular reflecting compartment.
 *
 * <p>Each acquisition interval is divided into small Brownian proposals. A
 * proposal outside the circle is rejected, which converges to a reflecting
 * boundary as the substep duration decreases.</p>
 */
public final class ConfinedDiffusion implements MotionModel {

    private final ConfinedParameters parameters;

    public ConfinedDiffusion(ConfinedParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Confined parameters cannot be null.");
        }
        this.parameters = parameters;
    }

    @Override
    public MotionType getType() {
        return MotionType.CONFINED;
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
        double substepDuration = context.getFrameIntervalSeconds()
                / parameters.getSubstepsPerFrame();
        double substepSigma = Math.sqrt(
                2.0 * parameters.getDiffusionUm2PerSecond() * substepDuration
        );
        double radiusSquared = parameters.getRadiusUm() * parameters.getRadiusUm();
        Random random = new Random(context.getRandomSeed());

        double currentX = 0.0;
        double currentY = 0.0;
        for (int frame = 1; frame < context.getFrames(); frame++) {
            for (int substep = 0;
                 substep < parameters.getSubstepsPerFrame();
                 substep++) {
                double proposedX = currentX + substepSigma * random.nextGaussian();
                double proposedY = currentY + substepSigma * random.nextGaussian();
                if (proposedX * proposedX + proposedY * proposedY <= radiusSquared) {
                    currentX = proposedX;
                    currentY = proposedY;
                }
            }
            x[frame] = currentX;
            y[frame] = currentY;
        }
        return new Trajectory(x, y);
    }

    private static void requireContext(MotionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Motion context cannot be null.");
        }
    }
}
