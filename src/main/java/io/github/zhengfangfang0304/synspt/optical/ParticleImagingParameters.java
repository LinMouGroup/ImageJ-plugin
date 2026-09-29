package io.github.zhengfangfang0304.synspt.optical;

/** Immutable optical properties assigned to one particle. */
public final class ParticleImagingParameters {

    private final ParticlePsfParameters psfParameters;
    private final double emitterPhotonsPerFrame;

    public ParticleImagingParameters(
            ParticlePsfParameters psfParameters,
            double emitterPhotonsPerFrame
    ) {
        if (psfParameters == null) {
            throw new IllegalArgumentException("Particle PSF parameters cannot be null.");
        }
        if (Double.isNaN(emitterPhotonsPerFrame)
                || Double.isInfinite(emitterPhotonsPerFrame)
                || emitterPhotonsPerFrame <= 0.0) {
            throw new IllegalArgumentException(
                    "Emitter photons per frame must be finite and positive."
            );
        }
        this.psfParameters = psfParameters;
        this.emitterPhotonsPerFrame = emitterPhotonsPerFrame;
    }

    public ParticlePsfParameters getPsfParameters() {
        return psfParameters;
    }

    public double getEmitterPhotonsPerFrame() {
        return emitterPhotonsPerFrame;
    }
}
