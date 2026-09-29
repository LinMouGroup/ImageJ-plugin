package io.github.zhengfangfang0304.synspt.noise;

import java.util.Arrays;

/** Immutable unsigned camera ADU values stored in a Java short array. */
public final class CameraFrame {

    private final int width;
    private final int height;
    private final int bitDepth;
    private final short[] pixels;

    public CameraFrame(int width, int height, int bitDepth, short[] pixels) {
        this(width, height, bitDepth, pixels, true);
    }

    static CameraFrame takeOwnership(
            int width,
            int height,
            int bitDepth,
            short[] pixels
    ) {
        return new CameraFrame(width, height, bitDepth, pixels, false);
    }

    private CameraFrame(
            int width,
            int height,
            int bitDepth,
            short[] pixels,
            boolean copy
    ) {
        if (width <= 0 || height <= 0 || (long) width * height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Camera dimensions are invalid.");
        }
        if (bitDepth != 8 && bitDepth != 16) {
            throw new IllegalArgumentException("Camera bit depth must be 8 or 16.");
        }
        if (pixels == null || pixels.length != width * height) {
            throw new IllegalArgumentException(
                    "Camera pixel array must match the frame dimensions."
            );
        }
        int maximumAdu = (1 << bitDepth) - 1;
        for (short pixel : pixels) {
            if ((pixel & 0xffff) > maximumAdu) {
                throw new IllegalArgumentException(
                        "Camera pixel exceeds the configured bit depth."
                );
            }
        }
        this.width = width;
        this.height = height;
        this.bitDepth = bitDepth;
        this.pixels = copy ? Arrays.copyOf(pixels, pixels.length) : pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBitDepth() {
        return bitDepth;
    }

    public int getUnsignedAdu(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Pixel coordinate is outside the frame.");
        }
        return pixels[y * width + x] & 0xffff;
    }

    public short[] copyPixels() {
        return Arrays.copyOf(pixels, pixels.length);
    }
}
