package io.github.zhengfangfang0304.particletracking.simulation.motion;

/**
 * Immutable final motion configuration assigned to one particle.
 *
 * <p>The assignment intentionally contains no trajectory samples, frame
 * state, or imaging information. Its seed is reserved for generating that
 * particle's single-state trajectory reproducibly.</p>
 */
public final class ParticleMotionAssignment {

    private final MotionProfile motionProfile;

    private final long trajectorySeed;

    /**
     * Creates a particle-level motion assignment.
     *
     * @param motionProfile resolved mathematical motion profile
     * @param trajectorySeed seed dedicated to this particle trajectory
     */
    public ParticleMotionAssignment(
            MotionProfile motionProfile,
            long trajectorySeed
    ) {
        if (motionProfile == null) {
            throw new IllegalArgumentException(
                    "Motion profile cannot be null."
            );
        }
        this.motionProfile = motionProfile;
        this.trajectorySeed = trajectorySeed;
    }

    public MotionProfile getMotionProfile() {
        return motionProfile;
    }

    public long getTrajectorySeed() {
        return trajectorySeed;
    }
}
