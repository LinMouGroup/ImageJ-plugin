package io.github.zhengfangfang0304.synspt.gui;

/** Fixed square image resolutions offered by the simulation GUI. */
public enum ResolutionPreset {

    RESOLUTION_128(128, 128),
    RESOLUTION_256(256, 256),
    RESOLUTION_512(512, 512),
    RESOLUTION_1024(1024, 1024);

    public static final ResolutionPreset DEFAULT = RESOLUTION_256;

    private final int width;
    private final int height;

    ResolutionPreset(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return width + " \u00d7 " + height;
    }
}
