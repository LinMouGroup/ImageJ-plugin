package io.github.zhengfangfang0304.synspt.config;

/**
 * Immutable microscope and camera configuration.
 *
 * <p>Pixel size and frame interval may be supplied by the generator GUI.
 * Background, read noise, gain, offset, and bit depth remain internally
 * configured physical camera parameters. All values are stored in metadata
 * for scientific reproducibility.</p>
 */
public final class MicroscopeConfig {

    public static final double DEFAULT_PIXEL_SIZE_UM = 0.1;
    public static final double DEFAULT_FRAME_INTERVAL_SECONDS = 1.0 / 30.0;
    public static final double DEFAULT_BACKGROUND_PHOTONS = 20.0;
    public static final double DEFAULT_READ_NOISE_SIGMA_ADU = 2.0;
    public static final double DEFAULT_GAIN_ADU_PER_PHOTON = 1.0;
    public static final double DEFAULT_OFFSET_ADU = 100.0;
    public static final int DEFAULT_BIT_DEPTH = 16;

    private final double pixelSizeUm;
    private final double frameIntervalSeconds;
    private final double backgroundPhotons;
    private final double readNoiseSigmaAdu;
    private final double gainAduPerPhoton;
    private final double offsetAdu;
    private final int bitDepth;

    public MicroscopeConfig(
            double pixelSizeUm,
            double frameIntervalSeconds,
            double backgroundPhotons,
            double readNoiseSigmaAdu,
            double gainAduPerPhoton,
            double offsetAdu,
            int bitDepth
    ) {
        requirePositive(pixelSizeUm, "Pixel size");
        requirePositive(frameIntervalSeconds, "Frame interval");
        requireNonNegative(backgroundPhotons, "Background photons");
        requireNonNegative(readNoiseSigmaAdu, "Read-noise sigma");
        requirePositive(gainAduPerPhoton, "Camera gain");
        requireNonNegative(offsetAdu, "Camera offset");
        if (bitDepth != 8 && bitDepth != 16) {
            throw new IllegalArgumentException("Bit depth must be 8 or 16.");
        }

        this.pixelSizeUm = pixelSizeUm;
        this.frameIntervalSeconds = frameIntervalSeconds;
        this.backgroundPhotons = backgroundPhotons;
        this.readNoiseSigmaAdu = readNoiseSigmaAdu;
        this.gainAduPerPhoton = gainAduPerPhoton;
        this.offsetAdu = offsetAdu;
        this.bitDepth = bitDepth;
    }

    public static MicroscopeConfig defaultConfig() {
        return new MicroscopeConfig(
                DEFAULT_PIXEL_SIZE_UM,
                DEFAULT_FRAME_INTERVAL_SECONDS,
                DEFAULT_BACKGROUND_PHOTONS,
                DEFAULT_READ_NOISE_SIGMA_ADU,
                DEFAULT_GAIN_ADU_PER_PHOTON,
                DEFAULT_OFFSET_ADU,
                DEFAULT_BIT_DEPTH
        );
    }

    public double getPixelSizeUm() {
        return pixelSizeUm;
    }

    public double getFrameIntervalSeconds() {
        return frameIntervalSeconds;
    }

    public double getBackgroundPhotons() {
        return backgroundPhotons;
    }

    public double getReadNoiseSigmaAdu() {
        return readNoiseSigmaAdu;
    }

    public double getGainAduPerPhoton() {
        return gainAduPerPhoton;
    }

    public double getOffsetAdu() {
        return offsetAdu;
    }

    public int getBitDepth() {
        return bitDepth;
    }

    private static void requirePositive(double value, String name) {
        if (!isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive.");
        }
    }

    private static void requireNonNegative(double value, String name) {
        if (!isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative.");
        }
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
