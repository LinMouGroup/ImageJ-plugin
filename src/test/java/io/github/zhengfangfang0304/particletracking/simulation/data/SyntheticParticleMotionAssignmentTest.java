package io.github.zhengfangfang0304.particletracking.simulation.data;

import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfile;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfileResolver;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;
import io.github.zhengfangfang0304.particletracking.simulation.motion.ParticleMotionAssignment;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SyntheticParticleMotionAssignmentTest {

    @Test
    void legacyConstructorRemainsCompatible() {
        SyntheticParticle particle = new SyntheticParticle(7);
        particle.addPosition(1, 12.5, 8.25);

        assertEquals(7, particle.getParticleId());
        assertNull(particle.getMotionAssignment());
        Point2D.Double position = particle.getTrajectory().get(1);
        assertEquals(12.5, position.x);
        assertEquals(8.25, position.y);
    }

    @Test
    void constructorStoresParticleMotionAssignment() {
        ParticleMotionAssignment assignment = assignment(
                MotionType.SUBDIFFUSION,
                13579L
        );

        SyntheticParticle particle = new SyntheticParticle(
                3,
                assignment
        );

        assertEquals(3, particle.getParticleId());
        assertSame(assignment, particle.getMotionAssignment());
        assertEquals(
                MotionType.SUBDIFFUSION,
                particle.getMotionAssignment()
                        .getMotionProfile()
                        .getMotionType()
        );
        assertEquals(
                13579L,
                particle.getMotionAssignment().getTrajectorySeed()
        );
    }

    @Test
    void assignmentCanBeAttachedWithoutChangingTrajectoryData() {
        SyntheticParticle particle = new SyntheticParticle(11);
        particle.addPosition(1, 4.0, 6.0);
        ParticleMotionAssignment assignment = assignment(
                MotionType.SUPERDIFFUSION,
                24680L
        );

        particle.setMotionAssignment(assignment);

        assertSame(assignment, particle.getMotionAssignment());
        assertEquals(1, particle.getTrajectory().size());
        assertEquals(4.0, particle.getTrajectory().get(1).x);
        assertEquals(6.0, particle.getTrajectory().get(1).y);
    }

    @Test
    void nullAssignmentIsRejected() {
        SyntheticParticle particle = new SyntheticParticle(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> particle.setMotionAssignment(null)
        );
    }

    private ParticleMotionAssignment assignment(
            MotionType motionType,
            long trajectorySeed
    ) {
        MotionProfile profile =
                new MotionProfileResolver().resolve(motionType);
        return new ParticleMotionAssignment(
                profile,
                trajectorySeed
        );
    }
}
