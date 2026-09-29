package io.github.zhengfangfang0304.synspt.motion.parameters;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Map;

/** Parameters for annealed transient-time motion. */
public final class AttmParameters implements MotionParameters {

    private static final double RELATION_TOLERANCE = 1.0e-12;

    private final double alpha;
    private final double diffusivityExponentSigma;
    private final double dwellExponentGamma;
    private final double maximumDiffusionUm2PerSecond;
    private final double minimumDwellTimeSeconds;

    public AttmParameters(
            double alpha,
            double diffusivityExponentSigma,
            double dwellExponentGamma,
            double maximumDiffusionUm2PerSecond,
            double minimumDwellTimeSeconds
    ) {
        ParameterSupport.requireOpenUnitInterval(alpha, "ATTM alpha");
        ParameterSupport.requirePositive(
                diffusivityExponentSigma,
                "ATTM sigma"
        );
        ParameterSupport.requirePositive(dwellExponentGamma, "ATTM gamma");
        if (!(diffusivityExponentSigma < dwellExponentGamma
                && dwellExponentGamma < diffusivityExponentSigma + 1.0)) {
            throw new IllegalArgumentException(
                    "ATTM requires sigma < gamma < sigma + 1."
            );
        }
        double derivedAlpha = diffusivityExponentSigma / dwellExponentGamma;
        if (Math.abs(alpha - derivedAlpha) > RELATION_TOLERANCE) {
            throw new IllegalArgumentException("ATTM requires alpha = sigma / gamma.");
        }
        ParameterSupport.requirePositive(
                maximumDiffusionUm2PerSecond,
                "Maximum diffusion coefficient"
        );
        ParameterSupport.requirePositive(
                minimumDwellTimeSeconds,
                "Minimum dwell time"
        );
        this.alpha = alpha;
        this.diffusivityExponentSigma = diffusivityExponentSigma;
        this.dwellExponentGamma = dwellExponentGamma;
        this.maximumDiffusionUm2PerSecond = maximumDiffusionUm2PerSecond;
        this.minimumDwellTimeSeconds = minimumDwellTimeSeconds;
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.ATTM;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getDiffusivityExponentSigma() {
        return diffusivityExponentSigma;
    }

    public double getDwellExponentGamma() {
        return dwellExponentGamma;
    }

    public double getMaximumDiffusionUm2PerSecond() {
        return maximumDiffusionUm2PerSecond;
    }

    public double getMinimumDwellTimeSeconds() {
        return minimumDwellTimeSeconds;
    }

    @Override
    public Map<String, String> toMetadata() {
        return ParameterSupport.metadata(
                "alpha", ParameterSupport.number(alpha),
                "sigma", ParameterSupport.number(diffusivityExponentSigma),
                "gamma", ParameterSupport.number(dwellExponentGamma),
                "D_max_um2_per_s",
                ParameterSupport.number(maximumDiffusionUm2PerSecond),
                "tau0_s", ParameterSupport.number(minimumDwellTimeSeconds)
        );
    }
}
