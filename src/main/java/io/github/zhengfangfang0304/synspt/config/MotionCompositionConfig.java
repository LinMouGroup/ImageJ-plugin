package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Immutable normalized composition of the motion models selected for one run. */
public final class MotionCompositionConfig {

    private static final double SUM_TOLERANCE = 1.0e-12;

    private final Map<MotionType, Double> composition;

    public MotionCompositionConfig(Map<MotionType, Double> composition) {
        if (composition == null || composition.isEmpty()) {
            throw new IllegalArgumentException(
                    "Motion composition cannot be null or empty."
            );
        }

        EnumMap<MotionType, Double> copy =
                new EnumMap<MotionType, Double>(MotionType.class);
        double sum = 0.0;
        for (Map.Entry<MotionType, Double> entry : composition.entrySet()) {
            MotionType motionType = entry.getKey();
            Double ratio = entry.getValue();
            if (motionType == null || ratio == null) {
                throw new IllegalArgumentException(
                        "Motion composition cannot contain null keys or values."
                );
            }
            if (Double.isNaN(ratio)
                    || Double.isInfinite(ratio)
                    || ratio <= 0.0) {
                throw new IllegalArgumentException(
                        "Motion composition ratios must be finite and positive."
                );
            }
            copy.put(motionType, ratio);
            sum += ratio;
        }
        if (Double.isNaN(sum)
                || Double.isInfinite(sum)
                || Math.abs(sum - 1.0) > SUM_TOLERANCE) {
            throw new IllegalArgumentException(
                    "Motion composition ratios must sum to one."
            );
        }
        this.composition = Collections.unmodifiableMap(copy);
    }

    public Map<MotionType, Double> getComposition() {
        return composition;
    }
}
