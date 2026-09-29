package io.github.zhengfangfang0304.synspt.config;

/**
 * Hidden, typed ranges used to sample independent parameters for all eight
 * motion models. Diffusion-like scale parameters are sampled log-uniformly;
 * exponents, angles, ratios, and speeds explicitly documented by samplers are
 * sampled uniformly unless the sampler states otherwise.
 */
public final class MotionParameterDistributionConfig {

    private final NumericRange brownianDiffusion;
    private final NumericRange directedDiffusion;
    private final NumericRange directedSpeed;
    private final NumericRange confinedDiffusion;
    private final NumericRange confinedRadius;
    private final int confinedSubsteps;
    private final NumericRange fbmHurst;
    private final NumericRange fbmGeneralizedDiffusion;
    private final NumericRange ctrwAlpha;
    private final NumericRange ctrwDiffusion;
    private final NumericRange ctrwWaitingTimeFactor;
    private final NumericRange levyWalkSigma;
    private final NumericRange levyWalkSpeed;
    private final NumericRange levyWalkFlightTimeFactor;
    private final NumericRange sbmAlpha;
    private final NumericRange sbmGeneralizedDiffusion;
    private final NumericRange attmSigma;
    private final NumericRange attmGammaOffset;
    private final NumericRange attmMaximumDiffusion;
    private final NumericRange attmDwellTimeFactor;

    public MotionParameterDistributionConfig(
            NumericRange brownianDiffusion,
            NumericRange directedDiffusion,
            NumericRange directedSpeed,
            NumericRange confinedDiffusion,
            NumericRange confinedRadius,
            int confinedSubsteps,
            NumericRange fbmHurst,
            NumericRange fbmGeneralizedDiffusion,
            NumericRange ctrwAlpha,
            NumericRange ctrwDiffusion,
            NumericRange ctrwWaitingTimeFactor,
            NumericRange levyWalkSigma,
            NumericRange levyWalkSpeed,
            NumericRange levyWalkFlightTimeFactor,
            NumericRange sbmAlpha,
            NumericRange sbmGeneralizedDiffusion,
            NumericRange attmSigma,
            NumericRange attmGammaOffset,
            NumericRange attmMaximumDiffusion,
            NumericRange attmDwellTimeFactor
    ) {
        NumericRange[] ranges = new NumericRange[] {
                brownianDiffusion, directedDiffusion, directedSpeed,
                confinedDiffusion, confinedRadius, fbmHurst,
                fbmGeneralizedDiffusion, ctrwAlpha, ctrwDiffusion,
                ctrwWaitingTimeFactor, levyWalkSigma, levyWalkSpeed,
                levyWalkFlightTimeFactor, sbmAlpha, sbmGeneralizedDiffusion,
                attmSigma, attmGammaOffset, attmMaximumDiffusion,
                attmDwellTimeFactor
        };
        for (NumericRange range : ranges) {
            if (range == null) {
                throw new IllegalArgumentException(
                        "Motion parameter ranges cannot be null."
                );
            }
        }
        if (confinedSubsteps <= 0) {
            throw new IllegalArgumentException(
                    "Confined substeps must be positive."
            );
        }
        requireInsideOpenUnitInterval(fbmHurst, "FBM Hurst range");
        requireInsideOpenUnitInterval(ctrwAlpha, "CTRW alpha range");
        if (levyWalkSigma.getMinimum() <= 1.0
                || levyWalkSigma.getMaximum() >= 2.0) {
            throw new IllegalArgumentException(
                    "Levy-walk sigma range must lie strictly inside (1, 2)."
            );
        }
        if (sbmAlpha.getMaximum() > 2.0) {
            throw new IllegalArgumentException("SBM alpha cannot exceed two.");
        }
        if (attmGammaOffset.getMaximum() >= 1.0) {
            throw new IllegalArgumentException(
                    "ATTM gamma offset must be below one."
            );
        }
        this.brownianDiffusion = brownianDiffusion;
        this.directedDiffusion = directedDiffusion;
        this.directedSpeed = directedSpeed;
        this.confinedDiffusion = confinedDiffusion;
        this.confinedRadius = confinedRadius;
        this.confinedSubsteps = confinedSubsteps;
        this.fbmHurst = fbmHurst;
        this.fbmGeneralizedDiffusion = fbmGeneralizedDiffusion;
        this.ctrwAlpha = ctrwAlpha;
        this.ctrwDiffusion = ctrwDiffusion;
        this.ctrwWaitingTimeFactor = ctrwWaitingTimeFactor;
        this.levyWalkSigma = levyWalkSigma;
        this.levyWalkSpeed = levyWalkSpeed;
        this.levyWalkFlightTimeFactor = levyWalkFlightTimeFactor;
        this.sbmAlpha = sbmAlpha;
        this.sbmGeneralizedDiffusion = sbmGeneralizedDiffusion;
        this.attmSigma = attmSigma;
        this.attmGammaOffset = attmGammaOffset;
        this.attmMaximumDiffusion = attmMaximumDiffusion;
        this.attmDwellTimeFactor = attmDwellTimeFactor;
    }

    public static MotionParameterDistributionConfig defaultConfig() {
        return new MotionParameterDistributionConfig(
                new NumericRange(0.01, 5.0),
                new NumericRange(0.001, 0.50),
                new NumericRange(0.20, 8.0),
                new NumericRange(0.01, 1.0),
                new NumericRange(0.05, 0.50),
                100,
                new NumericRange(0.10, 0.90),
                new NumericRange(0.01, 1.0),
                new NumericRange(0.20, 0.90),
                new NumericRange(0.01, 1.0),
                new NumericRange(0.50, 3.0),
                new NumericRange(1.10, 1.90),
                new NumericRange(0.20, 5.0),
                new NumericRange(0.50, 3.0),
                new NumericRange(0.20, 1.80),
                new NumericRange(0.01, 1.0),
                new NumericRange(0.50, 1.50),
                new NumericRange(0.05, 0.95),
                new NumericRange(0.02, 1.0),
                new NumericRange(0.50, 3.0)
        );
    }

    public NumericRange getBrownianDiffusion() { return brownianDiffusion; }
    public NumericRange getDirectedDiffusion() { return directedDiffusion; }
    public NumericRange getDirectedSpeed() { return directedSpeed; }
    public NumericRange getConfinedDiffusion() { return confinedDiffusion; }
    public NumericRange getConfinedRadius() { return confinedRadius; }
    public int getConfinedSubsteps() { return confinedSubsteps; }
    public NumericRange getFbmHurst() { return fbmHurst; }
    public NumericRange getFbmGeneralizedDiffusion() {
        return fbmGeneralizedDiffusion;
    }
    public NumericRange getCtrwAlpha() { return ctrwAlpha; }
    public NumericRange getCtrwDiffusion() { return ctrwDiffusion; }
    public NumericRange getCtrwWaitingTimeFactor() { return ctrwWaitingTimeFactor; }
    public NumericRange getLevyWalkSigma() { return levyWalkSigma; }
    public NumericRange getLevyWalkSpeed() { return levyWalkSpeed; }
    public NumericRange getLevyWalkFlightTimeFactor() {
        return levyWalkFlightTimeFactor;
    }
    public NumericRange getSbmAlpha() { return sbmAlpha; }
    public NumericRange getSbmGeneralizedDiffusion() {
        return sbmGeneralizedDiffusion;
    }
    public NumericRange getAttmSigma() { return attmSigma; }
    public NumericRange getAttmGammaOffset() { return attmGammaOffset; }
    public NumericRange getAttmMaximumDiffusion() { return attmMaximumDiffusion; }
    public NumericRange getAttmDwellTimeFactor() { return attmDwellTimeFactor; }

    private static void requireInsideOpenUnitInterval(
            NumericRange range,
            String name
    ) {
        if (range.getMinimum() <= 0.0 || range.getMaximum() >= 1.0) {
            throw new IllegalArgumentException(name + " must lie inside (0, 1).");
        }
    }

    /** Immutable inclusive finite positive sampling interval. */
    public static final class NumericRange {
        private final double minimum;
        private final double maximum;

        public NumericRange(double minimum, double maximum) {
            if (Double.isNaN(minimum)
                    || Double.isInfinite(minimum)
                    || Double.isNaN(maximum)
                    || Double.isInfinite(maximum)
                    || minimum <= 0.0
                    || maximum < minimum) {
                throw new IllegalArgumentException(
                        "Numeric range must be finite, positive, and ordered."
                );
            }
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public double getMinimum() { return minimum; }
        public double getMaximum() { return maximum; }
    }
}
