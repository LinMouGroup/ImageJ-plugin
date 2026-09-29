package io.github.zhengfangfang0304.particletracking.simulation.data;

import io.github.zhengfangfang0304.particletracking.simulation.motion.ParticleMotionAssignment;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores the ground-truth trajectory for one synthetic particle.
 */
public class SyntheticParticle {

    private final int particleId;

    private final Map<Integer, Point2D.Double> trajectory =
            new LinkedHashMap<>();

    private ParticleMotionAssignment motionAssignment;

    /**
     * Creates a particle without a motion assignment.
     *
     * <p>This constructor remains available for the legacy generation path.
     * A particle-level assignment can be attached before profile-driven
     * trajectory generation.</p>
     */
    public SyntheticParticle(int particleId) {
        this.particleId = particleId;
    }

    /**
     * Creates a particle with its final single-state motion assignment.
     *
     * @param particleId particle identifier
     * @param motionAssignment resolved particle-level motion assignment
     */
    public SyntheticParticle(
            int particleId,
            ParticleMotionAssignment motionAssignment
    ) {
        this(particleId);
        setMotionAssignment(motionAssignment);
    }

    public int getParticleId() {
        return particleId;
    }

    /**
     * Returns an unmodifiable view of frame-indexed particle positions.
     */
    public Map<Integer, Point2D.Double> getTrajectory() {
        return Collections.unmodifiableMap(trajectory);
    }

    /**
     * Returns this particle's motion assignment, or {@code null} when the
     * legacy construction path has not attached one yet.
     */
    public ParticleMotionAssignment getMotionAssignment() {
        return motionAssignment;
    }

    /**
     * Attaches the resolved single-state motion assignment to this particle.
     */
    public void setMotionAssignment(
            ParticleMotionAssignment motionAssignment
    ) {
        if (motionAssignment == null) {
            throw new IllegalArgumentException(
                    "Particle motion assignment cannot be null."
            );
        }
        this.motionAssignment = motionAssignment;
    }

    /**
     * Stores one ground-truth position.
     */
    public void addPosition(int frame, double x, double y) {
        trajectory.put(frame, new Point2D.Double(x, y));
    }
}
