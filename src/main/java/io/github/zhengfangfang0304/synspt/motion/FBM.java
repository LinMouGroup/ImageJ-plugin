package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.fbm.FractionalGaussianNoise;
import io.github.zhengfangfang0304.synspt.motion.parameters.FbmParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import java.util.Random;

/**
 * Two-dimensional fractional Brownian motion with independent Cartesian FGN.
 */
public final class FBM implements MotionModel {

    private final FbmParameters parameters;

    public FBM(FbmParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("FBM parameters cannot be null.");
        }
        this.parameters = parameters;
    }

    @Override
    public MotionType getType() {
        return MotionType.FBM;
    }

    @Override
    public MotionParameters getParameters() {
        return parameters;
    }

    @Override
    public Trajectory generate(MotionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Motion context cannot be null.");
        }

        int incrementCount = context.getFrames() - 1;
        Random random = new Random(context.getRandomSeed());
        FractionalGaussianNoise noise = new FractionalGaussianNoise(
                parameters.getHurstExponent()
        );
        double[] xIncrements = noise.generate(incrementCount, random);
        double[] yIncrements = noise.generate(incrementCount, random);
        double physicalScale = Math.sqrt(
                2.0 * parameters.getGeneralizedDiffusionUm2PerSecondAlpha()
        ) * Math.pow(
                context.getFrameIntervalSeconds(),
                parameters.getHurstExponent()
        );

        double[] x = cumulativePhysicalPath(xIncrements, physicalScale);
        double[] y = cumulativePhysicalPath(yIncrements, physicalScale);
        return new Trajectory(x, y);
    }

    private double[] cumulativePhysicalPath(double[] increments, double scale) {
        double[] path = new double[increments.length + 1];
        for (int index = 0; index < increments.length; index++) {
            path[index + 1] = path[index] + scale * increments[index];
        }
        return path;
    }
}
