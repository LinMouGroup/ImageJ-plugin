package io.github.zhengfangfang0304.synspt.particle;

/**
 * Associates one particle definition with its ground-truth trajectory.
 */
public final class ParticleTrack {

    private final Particle particle;
    private final Trajectory trajectory;

    public ParticleTrack(Particle particle, Trajectory trajectory) {
        if (particle == null || trajectory == null) {
            throw new IllegalArgumentException("Particle and trajectory cannot be null.");
        }
        this.particle = particle;
        this.trajectory = trajectory;
    }

    public Particle getParticle() {
        return particle;
    }

    public Trajectory getTrajectory() {
        return trajectory;
    }
}
