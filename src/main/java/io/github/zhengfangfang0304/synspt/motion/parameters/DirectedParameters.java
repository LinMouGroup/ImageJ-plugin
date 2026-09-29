package io.github.zhengfangfang0304.synspt.motion.parameters;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Map;

/** Parameters for Brownian diffusion with constant drift. */
public final class DirectedParameters implements MotionParameters {

    private static final double TWO_PI = 2.0 * Math.PI;

    private final double diffusionUm2PerSecond;
    private final double speedUmPerSecond;
    private final double directionRadians;

    public DirectedParameters(
            double diffusionUm2PerSecond,
            double speedUmPerSecond,
            double directionRadians
    ) {
        ParameterSupport.requireNonNegative(
                diffusionUm2PerSecond,
                "Diffusion coefficient"
        );
        ParameterSupport.requirePositive(speedUmPerSecond, "Directed speed");
        if (!ParameterSupport.isFinite(directionRadians)) {
            throw new IllegalArgumentException("Direction must be finite.");
        }
        this.diffusionUm2PerSecond = diffusionUm2PerSecond;
        this.speedUmPerSecond = speedUmPerSecond;
        this.directionRadians = normalizeAngle(directionRadians);
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.DIRECTED;
    }

    public double getDiffusionUm2PerSecond() {
        return diffusionUm2PerSecond;
    }

    public double getSpeedUmPerSecond() {
        return speedUmPerSecond;
    }

    public double getDirectionRadians() {
        return directionRadians;
    }

    @Override
    public Map<String, String> toMetadata() {
        return ParameterSupport.metadata(
                "D_um2_per_s", ParameterSupport.number(diffusionUm2PerSecond),
                "speed_um_per_s", ParameterSupport.number(speedUmPerSecond),
                "direction_rad", ParameterSupport.number(directionRadians)
        );
    }

    private static double normalizeAngle(double angle) {
        double normalized = angle % TWO_PI;
        return normalized < 0.0 ? normalized + TWO_PI : normalized;
    }
}
