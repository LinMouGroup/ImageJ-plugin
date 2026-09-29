package io.github.zhengfangfang0304.particletracking.simulation.generators;

import io.github.zhengfangfang0304.particletracking.simulation.config.MotionSelectionConfig;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfile;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;
import io.github.zhengfangfang0304.particletracking.simulation.motion.ParticleMotionAssignment;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticleMotionAssignerTest {

    private final ParticleMotionAssigner assigner =
            new ParticleMotionAssigner();

    @Test
    void singleTypeSelectionAssignsThatProfileToEveryParticle() {
        MotionSelectionConfig config = new MotionSelectionConfig(
                EnumSet.of(MotionType.NORMAL_DIFFUSION)
        );

        List<ParticleMotionAssignment> assignments =
                assigner.assign(10, config, 1234L);

        assertEquals(10, assignments.size());
        for (ParticleMotionAssignment assignment : assignments) {
            MotionProfile profile = assignment.getMotionProfile();
            assertEquals(
                    MotionType.NORMAL_DIFFUSION,
                    profile.getMotionType()
            );
            assertEquals("normal-diffusion", profile.getProfileId());
        }
    }

    @Test
    void multipleTypesAreAssignedWithBalancedCounts() {
        MotionSelectionConfig config = new MotionSelectionConfig(
                EnumSet.of(
                        MotionType.NORMAL_DIFFUSION,
                        MotionType.SUBDIFFUSION,
                        MotionType.SUPERDIFFUSION
                )
        );

        List<ParticleMotionAssignment> assignments =
                assigner.assign(10, config, 9876L);
        Map<MotionType, Integer> counts = new EnumMap<>(
                MotionType.class
        );
        for (ParticleMotionAssignment assignment : assignments) {
            MotionType motionType = assignment
                    .getMotionProfile()
                    .getMotionType();
            counts.merge(motionType, 1, Integer::sum);
        }

        assertEquals(4, counts.get(MotionType.NORMAL_DIFFUSION));
        assertEquals(3, counts.get(MotionType.SUBDIFFUSION));
        assertEquals(3, counts.get(MotionType.SUPERDIFFUSION));
        int minimum = counts.values().stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElseThrow();
        int maximum = counts.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElseThrow();
        assertTrue(maximum - minimum <= 1);
    }

    @Test
    void sameMasterSeedProducesIdenticalAssignments() {
        MotionSelectionConfig config = new MotionSelectionConfig(
                EnumSet.of(
                        MotionType.NORMAL_DIFFUSION,
                        MotionType.SUBDIFFUSION,
                        MotionType.SUPERDIFFUSION
                )
        );

        List<ParticleMotionAssignment> first =
                assigner.assign(10, config, 42L);
        List<ParticleMotionAssignment> second =
                assigner.assign(10, config, 42L);

        assertEquals(first.size(), second.size());
        for (int index = 0; index < first.size(); index++) {
            ParticleMotionAssignment firstAssignment = first.get(index);
            ParticleMotionAssignment secondAssignment = second.get(index);
            assertEquals(
                    firstAssignment.getMotionProfile().getProfileId(),
                    secondAssignment.getMotionProfile().getProfileId()
            );
            assertEquals(
                    firstAssignment.getTrajectorySeed(),
                    secondAssignment.getTrajectorySeed()
            );
        }
    }

    @Test
    void fewerParticlesThanSelectedTypesFailsValidation() {
        MotionSelectionConfig config = new MotionSelectionConfig(
                EnumSet.of(
                        MotionType.NORMAL_DIFFUSION,
                        MotionType.SUBDIFFUSION,
                        MotionType.SUPERDIFFUSION
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> assigner.assign(2, config, 123L)
        );
    }

    @Test
    void everyAssignmentContainsAProfileAndTrajectorySeed() {
        MotionSelectionConfig config = new MotionSelectionConfig(
                EnumSet.of(
                        MotionType.NORMAL_DIFFUSION,
                        MotionType.DIRECTED_ANOMALOUS_DIFFUSION
                )
        );

        List<ParticleMotionAssignment> assignments =
                assigner.assign(10, config, 2026L);
        Set<Long> trajectorySeeds = new HashSet<>();
        for (ParticleMotionAssignment assignment : assignments) {
            assertNotNull(assignment.getMotionProfile());
            trajectorySeeds.add(assignment.getTrajectorySeed());
        }

        assertEquals(assignments.size(), trajectorySeeds.size());
    }
}
