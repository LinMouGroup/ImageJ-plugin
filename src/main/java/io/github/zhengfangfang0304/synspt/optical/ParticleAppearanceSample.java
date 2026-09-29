package io.github.zhengfangfang0304.synspt.optical;

/** Immutable uncalibrated PSF and brightness-weight sample. */
public final class ParticleAppearanceSample {

    private final ParticlePsfParameters psfParameters;
    private final double brightnessWeight;

    public ParticleAppearanceSample(
            ParticlePsfParameters psfParameters,
            double brightnessWeight
    ) {
        if (psfParameters == null) {
            throw new IllegalArgumentException(
                    "Particle PSF parameters cannot be null."
            );
        }
        if (Double.isNaN(brightnessWeight)
                || Double.isInfinite(brightnessWeight)
                || brightnessWeight <= 0.0) {
            throw new IllegalArgumentException(
                    "Brightness weight must be finite and positive."
            );
        }
        this.psfParameters = psfParameters;
        this.brightnessWeight = brightnessWeight;
    }

    public ParticlePsfParameters getPsfParameters() {
        return psfParameters;
    }

    public double getBrightnessWeight() {
        return brightnessWeight;
    }
}
