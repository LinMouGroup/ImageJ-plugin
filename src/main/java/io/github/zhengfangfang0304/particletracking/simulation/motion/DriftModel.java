package io.github.zhengfangfang0304.particletracking.simulation.motion;

/**
 * Immutable description of deterministic particle drift.
 *
 * <p>Velocity components are expressed in micrometres per second. The
 * model is independent of GUI and image calibration.</p>
 */
public final class DriftModel {

    /**
     * Drift variants supported by the first unified-FBM phase.
     */
    public enum Type {
        NONE,
        CONSTANT_DRIFT
    }

    public static final DriftModel NONE =
            new DriftModel(Type.NONE, 0.0, 0.0);

    private final Type type;

    private final double velocityXUmPerSecond;

    private final double velocityYUmPerSecond;

    private DriftModel(
            Type type,
            double velocityXUmPerSecond,
            double velocityYUmPerSecond
    ) {
        this.type = type;
        this.velocityXUmPerSecond = velocityXUmPerSecond;
        this.velocityYUmPerSecond = velocityYUmPerSecond;
    }

    public static DriftModel none() {
        return NONE;
    }

    /**
     * Creates a non-zero constant physical drift.
     */
    public static DriftModel constantDrift(
            double velocityXUmPerSecond,
            double velocityYUmPerSecond
    ) {
        if (!Double.isFinite(velocityXUmPerSecond)
                || !Double.isFinite(velocityYUmPerSecond)) {
            throw new IllegalArgumentException(
                    "Drift velocity components must be finite."
            );
        }
        if (velocityXUmPerSecond == 0.0
                && velocityYUmPerSecond == 0.0) {
            throw new IllegalArgumentException(
                    "Constant drift must have a non-zero velocity."
            );
        }

        return new DriftModel(
                Type.CONSTANT_DRIFT,
                velocityXUmPerSecond,
                velocityYUmPerSecond
        );
    }

    public Type getType() {
        return type;
    }

    public double getVelocityXUmPerSecond() {
        return velocityXUmPerSecond;
    }

    public double getVelocityYUmPerSecond() {
        return velocityYUmPerSecond;
    }

    public boolean isNone() {
        return type == Type.NONE;
    }
}
