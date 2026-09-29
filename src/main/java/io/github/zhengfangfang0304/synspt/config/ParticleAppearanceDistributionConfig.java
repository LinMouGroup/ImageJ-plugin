package io.github.zhengfangfang0304.synspt.config;

/** Hidden distribution settings for randomized per-particle appearance. */
public final class ParticleAppearanceDistributionConfig {

    public static final double DEFAULT_ELLIPTICAL_ASPECT_RATIO_MIN = 0.60;
    public static final double DEFAULT_ELLIPTICAL_ASPECT_RATIO_MAX = 0.70;
    public static final double DEFAULT_PSF_CUTOFF_SIGMA = 5.0;
    public static final double DEFAULT_BRIGHTNESS_WEIGHT_MIN = 1000.0;
    public static final double DEFAULT_BRIGHTNESS_WEIGHT_MAX = 5000.0;
    public static final double DEFAULT_SIZE_BRIGHTNESS_CORRELATION = 0.25;

    private final PsfSizeMixtureConfig psfSizeMixtureConfig;
    private final double ellipticalAspectRatioMin;
    private final double ellipticalAspectRatioMax;
    private final double psfCutoffSigma;
    private final double brightnessWeightMin;
    private final double brightnessWeightMax;
    private final double sizeBrightnessCorrelation;

    public ParticleAppearanceDistributionConfig(
            PsfSizeMixtureConfig psfSizeMixtureConfig,
            double ellipticalAspectRatioMin,
            double ellipticalAspectRatioMax,
            double psfCutoffSigma,
            double brightnessWeightMin,
            double brightnessWeightMax,
            double sizeBrightnessCorrelation
    ) {
        if (psfSizeMixtureConfig == null) {
            throw new IllegalArgumentException(
                    "PSF-size mixture config cannot be null."
            );
        }
        requireRange(ellipticalAspectRatioMin,
                ellipticalAspectRatioMax, "Elliptical aspect ratio");
        if (ellipticalAspectRatioMax > 1.0) {
            throw new IllegalArgumentException(
                    "Elliptical aspect ratio cannot exceed one."
            );
        }
        requirePositive(psfCutoffSigma, "PSF cutoff sigma");
        requireRange(brightnessWeightMin, brightnessWeightMax,
                "Brightness weight");
        if (!isFinite(sizeBrightnessCorrelation)
                || sizeBrightnessCorrelation < -1.0
                || sizeBrightnessCorrelation > 1.0) {
            throw new IllegalArgumentException(
                    "Size-brightness correlation must be finite and in [-1, 1]."
            );
        }
        this.psfSizeMixtureConfig = psfSizeMixtureConfig;
        this.ellipticalAspectRatioMin = ellipticalAspectRatioMin;
        this.ellipticalAspectRatioMax = ellipticalAspectRatioMax;
        this.psfCutoffSigma = psfCutoffSigma;
        this.brightnessWeightMin = brightnessWeightMin;
        this.brightnessWeightMax = brightnessWeightMax;
        this.sizeBrightnessCorrelation = sizeBrightnessCorrelation;
    }

    public static ParticleAppearanceDistributionConfig defaultConfig() {
        return new ParticleAppearanceDistributionConfig(
                PsfSizeMixtureConfig.defaultConfig(),
                DEFAULT_ELLIPTICAL_ASPECT_RATIO_MIN,
                DEFAULT_ELLIPTICAL_ASPECT_RATIO_MAX,
                DEFAULT_PSF_CUTOFF_SIGMA,
                DEFAULT_BRIGHTNESS_WEIGHT_MIN,
                DEFAULT_BRIGHTNESS_WEIGHT_MAX,
                DEFAULT_SIZE_BRIGHTNESS_CORRELATION
        );
    }

    public PsfSizeMixtureConfig getPsfSizeMixtureConfig() {
        return psfSizeMixtureConfig;
    }

    public double getEllipticalAspectRatioMin() {
        return ellipticalAspectRatioMin;
    }

    public double getEllipticalAspectRatioMax() {
        return ellipticalAspectRatioMax;
    }

    public double getPsfCutoffSigma() {
        return psfCutoffSigma;
    }

    public double getBrightnessWeightMin() {
        return brightnessWeightMin;
    }

    public double getBrightnessWeightMax() {
        return brightnessWeightMax;
    }

    public double getSizeBrightnessCorrelation() {
        return sizeBrightnessCorrelation;
    }

    private static void requireRange(double minimum, double maximum, String name) {
        requirePositive(minimum, name + " minimum");
        requirePositive(maximum, name + " maximum");
        if (minimum > maximum) {
            throw new IllegalArgumentException(
                    name + " minimum cannot exceed maximum."
            );
        }
    }

    private static void requirePositive(double value, String name) {
        if (!isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(
                    name + " must be finite and positive."
            );
        }
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
