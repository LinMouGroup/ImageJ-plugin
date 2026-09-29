package io.github.zhengfangfang0304.particletracking.simulation.motion;

/**
 * Immutable internal parameters resolved for one semantic motion type.
 *
 * <p>A profile contains model parameters only. Imaging calibration, image
 * dimensions, and particle identity belong to other layers.</p>
 */
public final class MotionProfile {

    private final String profileId;

    private final String profileVersion;

    private final MotionType motionType;

    private final double hurstExponent;

    private final double generalizedDiffusionCoefficient;

    private final DriftModel driftModel;

    public MotionProfile(
            String profileId,
            String profileVersion,
            MotionType motionType,
            double hurstExponent,
            double generalizedDiffusionCoefficient,
            DriftModel driftModel
    ) {
        if (profileId == null || profileId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Profile ID cannot be blank."
            );
        }
        if (profileVersion == null
                || profileVersion.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Profile version cannot be blank."
            );
        }
        if (motionType == null) {
            throw new IllegalArgumentException(
                    "Motion type cannot be null."
            );
        }
        if (!Double.isFinite(hurstExponent)
                || !(hurstExponent > 0.0
                && hurstExponent < 1.0)) {
            throw new IllegalArgumentException(
                    "Hurst exponent must be finite and between zero and one."
            );
        }
        if (!Double.isFinite(generalizedDiffusionCoefficient)
                || generalizedDiffusionCoefficient < 0.0) {
            throw new IllegalArgumentException(
                    "Generalized diffusion coefficient must be finite "
                            + "and non-negative."
            );
        }
        if (motionType == MotionType.IMMOBILE
                && generalizedDiffusionCoefficient != 0.0) {
            throw new IllegalArgumentException(
                    "Immobile motion must have a zero generalized "
                            + "diffusion coefficient."
            );
        }
        if (motionType != MotionType.IMMOBILE
                && generalizedDiffusionCoefficient == 0.0) {
            throw new IllegalArgumentException(
                    "Non-immobile motion must have a positive generalized "
                            + "diffusion coefficient."
            );
        }
        if (driftModel == null) {
            throw new IllegalArgumentException(
                    "Drift model cannot be null."
            );
        }

        this.profileId = profileId;
        this.profileVersion = profileVersion;
        this.motionType = motionType;
        this.hurstExponent = hurstExponent;
        this.generalizedDiffusionCoefficient =
                generalizedDiffusionCoefficient;
        this.driftModel = driftModel;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getProfileVersion() {
        return profileVersion;
    }

    public MotionType getMotionType() {
        return motionType;
    }

    public double getHurstExponent() {
        return hurstExponent;
    }

    /**
     * Returns the anomalous exponent derived from {@code alpha = 2H}.
     */
    public double getAlpha() {
        return 2.0 * hurstExponent;
    }

    /**
     * Returns {@code K_H} in {@code um^2 / s^(2H)}.
     */
    public double getGeneralizedDiffusionCoefficient() {
        return generalizedDiffusionCoefficient;
    }

    public DriftModel getDriftModel() {
        return driftModel;
    }
}
