package io.github.zhengfangfang0304.particletracking.simulation.motion;

/**
 * Resolves user-facing motion types to fixed internal FBM profiles.
 *
 * <p>This class is the single source of truth for the first-phase Hurst
 * mappings. Generators and GUI code must consume resolved profiles rather
 * than duplicate these values.</p>
 */
public final class MotionProfileResolver {

    private static final String PROFILE_VERSION = "1.0";

    private static final double DEFAULT_GENERALIZED_DIFFUSION = 5.0;

    private static final double NORMAL_HURST = 0.5;

    private static final double SUBDIFFUSION_HURST = 0.3;

    private static final double SUPERDIFFUSION_HURST = 0.75;

    private static final double DIRECTED_ANOMALOUS_HURST = 0.75;

    private static final double IMMOBILE_HURST = 0.5;

    private static final double IMMOBILE_GENERALIZED_DIFFUSION = 0.0;

    private static final double DIRECTED_DRIFT_X_UM_PER_SECOND = 1.0;

    private static final double DIRECTED_DRIFT_Y_UM_PER_SECOND = 0.0;

    /**
     * Returns the fixed first-phase profile for one motion type.
     */
    public MotionProfile resolve(MotionType motionType) {
        if (motionType == null) {
            throw new IllegalArgumentException(
                    "Motion type cannot be null."
            );
        }

        return switch (motionType) {
            case NORMAL_DIFFUSION -> profile(
                    "normal-diffusion",
                    motionType,
                    NORMAL_HURST,
                    DriftModel.none()
            );
            case SUBDIFFUSION -> profile(
                    "subdiffusion",
                    motionType,
                    SUBDIFFUSION_HURST,
                    DriftModel.none()
            );
            case SUPERDIFFUSION -> profile(
                    "superdiffusion",
                    motionType,
                    SUPERDIFFUSION_HURST,
                    DriftModel.none()
            );
            case DIRECTED_ANOMALOUS_DIFFUSION -> profile(
                    "directed-anomalous-diffusion",
                    motionType,
                    DIRECTED_ANOMALOUS_HURST,
                    DriftModel.constantDrift(
                            DIRECTED_DRIFT_X_UM_PER_SECOND,
                            DIRECTED_DRIFT_Y_UM_PER_SECOND
                    )
            );
            case IMMOBILE -> profile(
                    "immobile",
                    motionType,
                    IMMOBILE_HURST,
                    IMMOBILE_GENERALIZED_DIFFUSION,
                    DriftModel.none()
            );
        };
    }

    private MotionProfile profile(
            String profileId,
            MotionType motionType,
            double hurstExponent,
            DriftModel driftModel
    ) {
        return profile(
                profileId,
                motionType,
                hurstExponent,
                DEFAULT_GENERALIZED_DIFFUSION,
                driftModel
        );
    }

    private MotionProfile profile(
            String profileId,
            MotionType motionType,
            double hurstExponent,
            double generalizedDiffusionCoefficient,
            DriftModel driftModel
    ) {
        return new MotionProfile(
                profileId,
                PROFILE_VERSION,
                motionType,
                hurstExponent,
                generalizedDiffusionCoefficient,
                driftModel
        );
    }
}
