package io.github.zhengfangfang0304.synspt.optical;

import java.util.Arrays;

/** Immutable row-major expected photon counts for one exposure. */
public final class ExpectedPhotonFrame {

    private final int width;
    private final int height;
    private final double[] expectedPhotons;

    public ExpectedPhotonFrame(int width, int height, double[] expectedPhotons) {
        this(width, height, expectedPhotons, true);
    }

    static ExpectedPhotonFrame takeOwnership(
            int width,
            int height,
            double[] expectedPhotons
    ) {
        return new ExpectedPhotonFrame(width, height, expectedPhotons, false);
    }

    private ExpectedPhotonFrame(
            int width,
            int height,
            double[] expectedPhotons,
            boolean copy
    ) {
        int pixelCount = checkedPixelCount(width, height);
        if (expectedPhotons == null || expectedPhotons.length != pixelCount) {
            throw new IllegalArgumentException(
                    "Expected-photon array must match the frame dimensions."
            );
        }
        for (double value : expectedPhotons) {
            if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
                throw new IllegalArgumentException(
                        "Expected photon counts must be finite and non-negative."
                );
            }
        }
        this.width = width;
        this.height = height;
        this.expectedPhotons = copy
                ? Arrays.copyOf(expectedPhotons, expectedPhotons.length)
                : expectedPhotons;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getExpectedPhotons(int x, int y) {
        return expectedPhotons[index(x, y)];
    }

    public double getExpectedPhotons(int pixelIndex) {
        if (pixelIndex < 0 || pixelIndex >= expectedPhotons.length) {
            throw new IndexOutOfBoundsException("Pixel index is outside the frame.");
        }
        return expectedPhotons[pixelIndex];
    }

    public int getPixelCount() {
        return expectedPhotons.length;
    }

    public double[] copyExpectedPhotons() {
        return Arrays.copyOf(expectedPhotons, expectedPhotons.length);
    }

    private int index(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Pixel coordinate is outside the frame.");
        }
        return y * width + x;
    }

    private static int checkedPixelCount(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Frame dimensions must be positive.");
        }
        long pixelCount = (long) width * height;
        if (pixelCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Frame contains too many pixels.");
        }
        return (int) pixelCount;
    }
}
