package io.github.zhengfangfang0304.particletracking.simulation.config;

/**
 * Physical calibration of the simulated microscopy acquisition.
 *
 * <p>These values describe sampling by the microscope rather than the
 * stochastic motion model.</p>
 */
public class ImagingConfig {

    public static final double DEFAULT_PIXEL_SIZE_UM_PER_PIXEL = 0.1;

    public static final double DEFAULT_FRAME_INTERVAL_SECONDS = 0.05;

    private final double pixelSizeUmPerPixel;

    private final double frameIntervalSeconds;

    public ImagingConfig() {
        this(
                DEFAULT_PIXEL_SIZE_UM_PER_PIXEL,
                DEFAULT_FRAME_INTERVAL_SECONDS
        );
    }

    /**
     * Creates an immutable acquisition calibration.
     *
     * @param pixelSizeUmPerPixel physical size of one image pixel
     * @param frameIntervalSeconds elapsed time between consecutive frames
     */
    public ImagingConfig(
            double pixelSizeUmPerPixel,
            double frameIntervalSeconds
    ) {
        if (!Double.isFinite(pixelSizeUmPerPixel)
                || pixelSizeUmPerPixel <= 0.0) {
            throw new IllegalArgumentException(
                    "Pixel size must be finite and greater than zero."
            );
        }
        if (!Double.isFinite(frameIntervalSeconds)
                || frameIntervalSeconds <= 0.0) {
            throw new IllegalArgumentException(
                    "Frame interval must be finite and greater than zero."
            );
        }

        this.pixelSizeUmPerPixel = pixelSizeUmPerPixel;
        this.frameIntervalSeconds = frameIntervalSeconds;
    }

    //外界想要获取参数，只能调用这两个public方法，不能直接访问成员变量。
    public double getPixelSizeUmPerPixel() {
        return pixelSizeUmPerPixel;
    }

    public double getFrameIntervalSeconds() {
        return frameIntervalSeconds;
    }
}
