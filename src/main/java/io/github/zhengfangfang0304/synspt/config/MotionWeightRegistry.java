package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Immutable registry of the internal default weight for every motion model.
 *
 * <p>The no-argument constructor uses the application defaults. A complete
 * alternative map can be supplied to support testing and future advanced
 * configuration without exposing composition controls in the GUI.</p>
 */
public final class MotionWeightRegistry {

    private static final Map<MotionType, Double> APPLICATION_DEFAULTS =
            createApplicationDefaults();

    private final Map<MotionType, Double> defaultWeights;

    /** Creates a registry containing the application default weights. */
    public MotionWeightRegistry() {
        this(APPLICATION_DEFAULTS);
    }

    /**
     * Creates a registry from a complete set of positive finite weights.
     *
     * @param weights one weight for every {@link MotionType}
     */
    public MotionWeightRegistry(Map<MotionType, Double> weights) {
        if (weights == null) {
            throw new IllegalArgumentException("Motion weights cannot be null.");
        }

        EnumMap<MotionType, Double> copy =
                new EnumMap<MotionType, Double>(MotionType.class);
        for (MotionType motionType : MotionType.values()) {
            Double weight = weights.get(motionType);
            if (weight == null) {
                throw new IllegalArgumentException(
                        "A weight is required for every motion type."
                );
            }
            validateWeight(weight);
            copy.put(motionType, weight);
        }
        if (weights.size() != MotionType.values().length) {
            throw new IllegalArgumentException(
                    "Motion weights may contain only known motion types."
            );
        }
        this.defaultWeights = Collections.unmodifiableMap(copy);
    }

    /** Returns all default weights as an unmodifiable map. */
    public Map<MotionType, Double> getDefaultWeights() {
        return defaultWeights;
    }

    /** Returns the default weight of one motion type. */
    public double getWeight(MotionType motionType) {
        if (motionType == null) {
            throw new IllegalArgumentException("Motion type cannot be null.");
        }
        return defaultWeights.get(motionType);
    }

    private static void validateWeight(double weight) {
        if (Double.isNaN(weight)
                || Double.isInfinite(weight)
                || weight <= 0.0) {
            throw new IllegalArgumentException(
                    "Motion weights must be finite and positive."
            );
        }
    }

    private static Map<MotionType, Double> createApplicationDefaults() {
        EnumMap<MotionType, Double> weights =
                new EnumMap<MotionType, Double>(MotionType.class);
        weights.put(MotionType.BROWNIAN, 40.0);
        weights.put(MotionType.DIRECTED, 10.0);
        weights.put(MotionType.CONFINED, 15.0);
        weights.put(MotionType.FBM, 15.0);
        weights.put(MotionType.CTRW, 10.0);
        weights.put(MotionType.LEVY_WALK, 5.0);
        weights.put(MotionType.SBM, 3.0);
        weights.put(MotionType.ATTM, 2.0);
        weights.put(MotionType.MULTI_STATE, 10.0);
        return Collections.unmodifiableMap(weights);
    }
}
