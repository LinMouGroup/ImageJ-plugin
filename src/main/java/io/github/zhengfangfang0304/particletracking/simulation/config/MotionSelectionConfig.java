package io.github.zhengfangfang0304.particletracking.simulation.config;

import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Immutable simulation-level selection of candidate particle motion types.
 *
 * <p>This configuration stores only user-facing motion semantics and the
 * assignment strategy. Mathematical parameters remain the responsibility of
 * the motion profile resolver.</p>
 */
public final class MotionSelectionConfig {

    /**
     * Strategies available for assigning selected types to particles.
     */
    public enum AssignmentStrategy {

        /** Assign each selected type an approximately equal random share. */
        BALANCED_RANDOM
    }

    private final Set<MotionType> selectedMotionTypes;

    private final AssignmentStrategy assignmentStrategy;

    /**
     * Creates a selection using the default balanced-random strategy.
     *
     * @param selectedMotionTypes candidate motion types
     */
    public MotionSelectionConfig(Set<MotionType> selectedMotionTypes) {
        this(selectedMotionTypes, AssignmentStrategy.BALANCED_RANDOM);
    }

    /**
     * Creates a validated motion selection.
     *
     * @param selectedMotionTypes candidate motion types
     * @param assignmentStrategy particle assignment strategy
     */
    public MotionSelectionConfig(
            Set<MotionType> selectedMotionTypes,
            AssignmentStrategy assignmentStrategy
    ) {
        validateSelection(selectedMotionTypes, assignmentStrategy);
        this.selectedMotionTypes = Collections.unmodifiableSet(
                EnumSet.copyOf(selectedMotionTypes)
        );
        this.assignmentStrategy = assignmentStrategy;
    }

    public Set<MotionType> getSelectedMotionTypes() {
        return selectedMotionTypes;
    }

    public AssignmentStrategy getAssignmentStrategy() {
        return assignmentStrategy;
    }

    /**
     * Revalidates this configuration.
     */
    public void validate() {
        validateSelection(selectedMotionTypes, assignmentStrategy);
    }

    private static void validateSelection(
            Set<MotionType> selectedMotionTypes,
            AssignmentStrategy assignmentStrategy
    ) {
        if (selectedMotionTypes == null
                || selectedMotionTypes.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one motion type must be selected."
            );
        }
        if (selectedMotionTypes.contains(null)) {
            throw new IllegalArgumentException(
                    "Selected motion types cannot contain null."
            );
        }
        if (assignmentStrategy == null) {
            throw new IllegalArgumentException(
                    "Assignment strategy cannot be null."
            );
        }
    }
}
