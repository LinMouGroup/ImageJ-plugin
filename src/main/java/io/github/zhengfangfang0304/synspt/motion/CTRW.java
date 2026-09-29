package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.CtrwParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import java.util.Random;

/**
 * Subdiffusive continuous-time random walk sampled at regular frame times.
 *
 * <p>A single event clock is shared by both spatial axes. The position is
 * constant between events; at an event, both coordinates receive an
 * independent Gaussian jump.</p>
 */
public final class CTRW implements MotionModel {

    private final CtrwParameters parameters;

    public CTRW(CtrwParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("CTRW parameters cannot be null.");
        }
        this.parameters = parameters;
    }

    @Override
    public MotionType getType() {
        return MotionType.CTRW;
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
        double jumpSigma = Math.sqrt(
                2.0
                        * parameters.getDiffusionUm2PerSecond()
                        * parameters.getMinimumWaitingTimeSeconds()
        );

        double currentX = 0.0;
        double currentY = 0.0;
        double nextEventTime = drawWaitingTime(random);
        for (int frame = 1; frame < context.getFrames(); frame++) {
            double observationTime = frame * context.getFrameIntervalSeconds();
            while (nextEventTime <= observationTime) {
                currentX += jumpSigma * random.nextGaussian();
                currentY += jumpSigma * random.nextGaussian();
                double waitingTime = drawWaitingTime(random);
                if (Double.isInfinite(waitingTime)) {
                    nextEventTime = Double.POSITIVE_INFINITY;
                    break;
                }
                nextEventTime += waitingTime;
            }
            x[frame] = currentX;
            y[frame] = currentY;
        }
        return new Trajectory(x, y);
    }

    private double drawWaitingTime(Random random) {
        double uniformOpenClosed = 1.0 - random.nextDouble();
        return parameters.getMinimumWaitingTimeSeconds()
                * Math.pow(uniformOpenClosed, -1.0 / parameters.getAlpha());
    }

    private static void requireContext(MotionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Motion context cannot be null.");
        }
    }
}
