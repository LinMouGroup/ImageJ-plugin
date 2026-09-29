package io.github.zhengfangfang0304.synspt.util;

/**
 * Derives independent deterministic random streams from one simulation seed.
 *
 * <p>Domain separation prevents a model parameter draw from shifting the
 * trajectory or initial-position streams of later particles.</p>
 */
public final class SeedDerivation {

    private static final long GOLDEN_GAMMA = 0x9E3779B97F4A7C15L;
    private static final long MOTION_ASSIGNMENT_DOMAIN = 0x3C79AC492BA7B653L;
    private static final long MOTION_PARAMETER_DOMAIN = 0x8CB92BA72F3D8DD7L;
    private static final long TRAJECTORY_DOMAIN = 0x58F38DED92D9B32FL;
    private static final long POSITION_DOMAIN = 0x94D049BB133111EBL;
    private static final long CAMERA_NOISE_DOMAIN = 0xDB4F0B9175AE2165L;
    private static final long PSF_SIZE_COMPOSITION_DOMAIN = 0xA0761D6478BD642FL;
    private static final long PSF_SIZE_ASSIGNMENT_DOMAIN = 0xD6E8FEB86659FD93L;
    private static final long PSF_GEOMETRY_DOMAIN = 0xC6BC279692B5CC83L;
    private static final long BRIGHTNESS_DOMAIN = 0xAEF17502108EF2D9L;
    private static final long COMPOSITION_DOMAIN = 0xE7037ED1A0B428DBL;
    private static final long MULTI_STATE_COUNT_DOMAIN = 0x8EBC6AF09C88C6E3L;
    private static final long MULTI_STATE_TYPE_DOMAIN = 0x589965CC75374CC3L;
    private static final long MULTI_STATE_CHANGE_POINT_DOMAIN = 0x1D8E4E27C47D124FL;
    private static final long MULTI_STATE_PARAMETER_DOMAIN = 0xEB44ACCAB455D165L;
    private static final long MULTI_STATE_TRAJECTORY_DOMAIN = 0xA4093822299F31D0L;

    private SeedDerivation() {
    }

    public static long motionCompositionSeed(long rootSeed) {
        return derive(rootSeed, 0, COMPOSITION_DOMAIN);
    }

    public static long motionAssignmentSeed(long rootSeed) {
        return derive(rootSeed, 0, MOTION_ASSIGNMENT_DOMAIN);
    }

    public static long motionParameterSeed(long rootSeed, int particleId) {
        requireParticleId(particleId);
        return derive(rootSeed, particleId, MOTION_PARAMETER_DOMAIN);
    }

    public static long trajectorySeed(long rootSeed, int particleId) {
        requireParticleId(particleId);
        return derive(rootSeed, particleId, TRAJECTORY_DOMAIN);
    }

    public static long multiStateCountSeed(long rootSeed, int particleId) {
        requireParticleId(particleId);
        return derive(rootSeed, particleId, MULTI_STATE_COUNT_DOMAIN);
    }

    public static long multiStateTypeSeed(long rootSeed, int particleId) {
        requireParticleId(particleId);
        return derive(rootSeed, particleId, MULTI_STATE_TYPE_DOMAIN);
    }

    public static long multiStateChangePointSeed(
            long rootSeed,
            int particleId
    ) {
        requireParticleId(particleId);
        return derive(rootSeed, particleId, MULTI_STATE_CHANGE_POINT_DOMAIN);
    }

    public static long multiStateParameterSeed(
            long rootSeed,
            int particleId,
            int stateIndex
    ) {
        requireParticleId(particleId);
        requireStateIndex(stateIndex);
        return derive(
                derive(rootSeed, particleId, MULTI_STATE_PARAMETER_DOMAIN),
                stateIndex,
                MULTI_STATE_PARAMETER_DOMAIN ^ GOLDEN_GAMMA
        );
    }

    public static long multiStateTrajectorySeed(
            long rootSeed,
            int particleId,
            int stateIndex
    ) {
        requireParticleId(particleId);
        requireStateIndex(stateIndex);
        return derive(
                derive(rootSeed, particleId, MULTI_STATE_TRAJECTORY_DOMAIN),
                stateIndex,
                MULTI_STATE_TRAJECTORY_DOMAIN ^ GOLDEN_GAMMA
        );
    }

    public static long psfSizeCompositionSeed(long rootSeed) {
        return derive(rootSeed, 0, PSF_SIZE_COMPOSITION_DOMAIN);
    }

    public static long psfSizeAssignmentSeed(long rootSeed) {
        return derive(rootSeed, 0, PSF_SIZE_ASSIGNMENT_DOMAIN);
    }

    public static long psfGeometrySeed(long rootSeed, int particleId) {
        requireParticleId(particleId);
        return derive(rootSeed, particleId, PSF_GEOMETRY_DOMAIN);
    }

    public static long brightnessSeed(long rootSeed, int particleId) {
        requireParticleId(particleId);
        return derive(rootSeed, particleId, BRIGHTNESS_DOMAIN);
    }

    public static long initialPositionSeed(long rootSeed, int particleId) {
        requireParticleId(particleId);
        return derive(rootSeed, particleId, POSITION_DOMAIN);
    }

    public static long cameraNoiseSeed(long rootSeed, int frameIndex) {
        if (frameIndex < 0) {
            throw new IllegalArgumentException("Frame index cannot be negative.");
        }
        return derive(rootSeed, frameIndex, CAMERA_NOISE_DOMAIN);
    }

    private static long derive(long rootSeed, int index, long domain) {
        long value = rootSeed ^ domain ^ (GOLDEN_GAMMA * (index + 1L));
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static void requireParticleId(int particleId) {
        if (particleId <= 0) {
            throw new IllegalArgumentException("Particle ID must be positive.");
        }
    }

    private static void requireStateIndex(int stateIndex) {
        if (stateIndex < 0) {
            throw new IllegalArgumentException("State index cannot be negative.");
        }
    }
}
