package io.github.zhengfangfang0304.synspt.scene;

/**
 * Reflects coordinates at both field-of-view edges, including arbitrarily
 * large overshoots through a triangle-wave mapping.
 */
public final class ReflectingBoundaryHandler implements BoundaryHandler {

    @Override
    public double apply(double coordinate, double upperExclusive) {
        if (!isFinite(coordinate)) {
            throw new IllegalArgumentException("Coordinate must be finite.");
        }
        if (!isFinite(upperExclusive) || upperExclusive <= 0.0) {
            throw new IllegalArgumentException(
                    "Boundary upper limit must be finite and positive."
            );
        }

        double period = 2.0 * upperExclusive;
        if (Double.isInfinite(period)) {
            throw new IllegalArgumentException("Boundary period must be finite.");
        }
        double wrapped = coordinate % period;
        if (wrapped < 0.0) {
            wrapped += period;
        }
        double reflected = wrapped <= upperExclusive
                ? wrapped
                : period - wrapped;
        if (reflected >= upperExclusive) {
            return Math.nextAfter(upperExclusive, Double.NEGATIVE_INFINITY);
        }
        return reflected;
    }

    private boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
