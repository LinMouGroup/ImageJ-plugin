package io.github.zhengfangfang0304.synspt.optical;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Immutable realized SMALL/MEDIUM/LARGE composition for one simulation. */
public final class PsfSizeComposition {

    private static final double SUM_TOLERANCE = 1.0e-9;

    private final Map<PsfSizeClass, Double> ratios;

    public PsfSizeComposition(Map<PsfSizeClass, Double> ratios) {
        if (ratios == null) {
            throw new IllegalArgumentException(
                    "PSF-size composition cannot be null."
            );
        }
        EnumMap<PsfSizeClass, Double> copy =
                new EnumMap<PsfSizeClass, Double>(PsfSizeClass.class);
        double sum = 0.0;
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            Double ratio = ratios.get(sizeClass);
            if (ratio == null || Double.isNaN(ratio.doubleValue())
                    || Double.isInfinite(ratio.doubleValue())
                    || ratio.doubleValue() <= 0.0) {
                throw new IllegalArgumentException(
                        "Every PSF size class must have a finite positive ratio."
                );
            }
            copy.put(sizeClass, ratio);
            sum += ratio.doubleValue();
        }
        if (ratios.size() != PsfSizeClass.values().length) {
            throw new IllegalArgumentException(
                    "PSF-size composition contains an unsupported class."
            );
        }
        if (Math.abs(sum - 1.0) > SUM_TOLERANCE) {
            throw new IllegalArgumentException(
                    "PSF-size composition ratios must sum to one."
            );
        }
        this.ratios = Collections.unmodifiableMap(copy);
    }

    public Map<PsfSizeClass, Double> getRatios() {
        return ratios;
    }

    public double getRatio(PsfSizeClass sizeClass) {
        if (sizeClass == null) {
            throw new IllegalArgumentException("PSF size class cannot be null.");
        }
        return ratios.get(sizeClass).doubleValue();
    }
}
