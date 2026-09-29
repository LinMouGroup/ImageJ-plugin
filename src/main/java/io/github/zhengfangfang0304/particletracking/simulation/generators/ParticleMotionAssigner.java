package io.github.zhengfangfang0304.particletracking.simulation.generators;

import io.github.zhengfangfang0304.particletracking.simulation.config.MotionSelectionConfig;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfile;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfileResolver;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;
import io.github.zhengfangfang0304.particletracking.simulation.motion.ParticleMotionAssignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Assigns selected motion types and independent trajectory seeds to particles.
 */
public final class ParticleMotionAssigner {

    private final MotionProfileResolver motionProfileResolver;

    public ParticleMotionAssigner() {
        this(new MotionProfileResolver());
    }

    /**
     * Creates an assigner with an explicit profile resolver.
     *
     * @param motionProfileResolver resolver for semantic motion types
     */
    public ParticleMotionAssigner(
            MotionProfileResolver motionProfileResolver
    ) {
        if (motionProfileResolver == null) {
            throw new IllegalArgumentException(
                    "Motion profile resolver cannot be null."
            );
        }
        this.motionProfileResolver = motionProfileResolver;
    }

    /**
     * Creates one reproducible single-state assignment per particle.
     *
     * <p>For balanced random assignment, every selected type receives either
     * {@code floor(particles / types)} or {@code ceil(particles / types)}
     * particles. The master seed controls both the shuffled particle-level
     * type order and the generated trajectory seeds.</p>
     *
     * @param particleNumber number of particles to assign
     * @param selectionConfig selected motion types and assignment strategy
     * @param masterSeed seed controlling this complete assignment operation
     * @return immutable assignments in particle-index order
     */
    public List<ParticleMotionAssignment> assign(
            int particleNumber,
            MotionSelectionConfig selectionConfig,
            long masterSeed
    ) {
        validateArguments(particleNumber, selectionConfig);

        return switch (selectionConfig.getAssignmentStrategy()) {
            case BALANCED_RANDOM -> assignBalancedRandom(
                    particleNumber,
                    selectionConfig,
                    masterSeed
            );
        };
    }

    private List<ParticleMotionAssignment> assignBalancedRandom(
            int particleNumber,
            MotionSelectionConfig selectionConfig,
            long masterSeed
    ) {
        List<MotionType> selectedTypes = new ArrayList<>(
                selectionConfig.getSelectedMotionTypes()
        );
        List<MotionType> particleTypes = balancedTypes(
                particleNumber,
                selectedTypes
        );
        Random random = new Random(masterSeed);
        Collections.shuffle(particleTypes, random);

        List<ParticleMotionAssignment> assignments =
                new ArrayList<>(particleNumber);
        for (MotionType motionType : particleTypes) {
            MotionProfile motionProfile =
                    motionProfileResolver.resolve(motionType);
            assignments.add(
                    new ParticleMotionAssignment(
                            motionProfile,
                            random.nextLong()
                    )
            );
        }

        return Collections.unmodifiableList(assignments);
    }

    private List<MotionType> balancedTypes(
            int particleNumber,
            List<MotionType> selectedTypes
    ) {
        int typeCount = selectedTypes.size();
        int baseCount = particleNumber / typeCount;
        int remainder = particleNumber % typeCount;
        List<MotionType> particleTypes =
                new ArrayList<>(particleNumber);

        for (int typeIndex = 0;
             typeIndex < typeCount;
             typeIndex++) {
            int count = baseCount
                    + (typeIndex < remainder ? 1 : 0);
            for (int assignmentIndex = 0;
                 assignmentIndex < count;
                 assignmentIndex++) {
                particleTypes.add(selectedTypes.get(typeIndex));
            }
        }

        return particleTypes;
    }

    private void validateArguments(
            int particleNumber,
            MotionSelectionConfig selectionConfig
    ) {
        if (particleNumber <= 0) {
            throw new IllegalArgumentException(
                    "Particle number must be greater than zero."
            );
        }
        if (selectionConfig == null) {
            throw new IllegalArgumentException(
                    "Motion selection config cannot be null."
            );
        }
        selectionConfig.validate();

        int selectedTypeCount =
                selectionConfig.getSelectedMotionTypes().size();
        if (particleNumber < selectedTypeCount) {
            throw new IllegalArgumentException(
                    "Particle number must be at least the number of "
                            + "selected motion types."
            );
        }
    }
}
