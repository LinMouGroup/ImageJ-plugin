package io.github.zhengfangfang0304.particletracking.simulation.config;

/**
 * Supported square image dimensions for synthetic microscopy datasets.
 */
public enum ImageSizePreset {

    SIZE_64(64),
    SIZE_128(128),
    SIZE_256(256),
    SIZE_512(512),
    SIZE_1024(1024);

    private final int pixels;

    ImageSizePreset(int pixels) {
        this.pixels = pixels;
    }

    public int getWidth() {
        return pixels;
    }

    public int getHeight() {
        return pixels;
    }

    @Override
    public String toString() {
        return pixels + " × " + pixels;
    }
}
