package io.github.zhengfangfang0304.synspt.noise;

/** Scientific definition used to calibrate and report the requested SNR. */
public enum SnrDefinition {

    /**
     * Brightest PSF-pixel signal divided by total noise in that pixel,
     * including signal shot noise, background shot noise, and physical
     * camera read noise.
     */
    PEAK_PIXEL_SIGNAL(
            "peak_pixel_signal_over_total_noise"
    );

    private final String metadataName;

    SnrDefinition(String metadataName) {
        this.metadataName = metadataName;
    }

    public String getMetadataName() {
        return metadataName;
    }
}
