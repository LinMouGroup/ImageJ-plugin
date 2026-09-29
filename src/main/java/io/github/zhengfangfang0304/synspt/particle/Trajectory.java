package io.github.zhengfangfang0304.synspt.particle;

import java.util.Arrays;

/**
 * Immutable two-dimensional trajectory in micrometres.
 */
public final class Trajectory {

    private final double[] xUm;
    private final double[] yUm;

    public Trajectory(double[] xUm, double[] yUm) {
        if (xUm == null || yUm == null) {
            throw new IllegalArgumentException("Trajectory axes cannot be null.");
        }
        if (xUm.length == 0 || xUm.length != yUm.length) {
            throw new IllegalArgumentException(
                    "Trajectory axes must have the same positive length."
            );
        }
        validateFinite(xUm, "X");
        validateFinite(yUm, "Y");
        this.xUm = Arrays.copyOf(xUm, xUm.length);
        this.yUm = Arrays.copyOf(yUm, yUm.length);
    }

    public int length() {
        return xUm.length;
    }

    public double getXUm(int frameIndex) {
        return xUm[frameIndex];
    }

    public double getYUm(int frameIndex) {
        return yUm[frameIndex];
    }

    public double[] copyXUm() {
        return Arrays.copyOf(xUm, xUm.length);
    }

    public double[] copyYUm() {
        return Arrays.copyOf(yUm, yUm.length);
    }

    private static void validateFinite(double[] values, String axisName) {
        for (double value : values) {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new IllegalArgumentException(
                        axisName + " trajectory values must be finite."
                );
            }
        }
    }
}
