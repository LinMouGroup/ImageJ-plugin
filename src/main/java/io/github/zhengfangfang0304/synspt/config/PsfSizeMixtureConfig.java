package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.optical.PsfSizeClass;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Hidden configuration for the randomized SMALL/MEDIUM/LARGE PSF mixture.
 *
 * <p>The Dirichlet parameters control the unconstrained prior, while the
 * proportion bounds prevent implausible simulations dominated by one size
 * class.</p>
 */
public final class PsfSizeMixtureConfig {

    public static final double DEFAULT_SMALL_SIGMA_MIN_PIXELS = 1.2;
    public static final double DEFAULT_SMALL_SIGMA_MAX_PIXELS = 1.5;
    public static final double DEFAULT_MEDIUM_SIGMA_MIN_PIXELS = 1.6;
    public static final double DEFAULT_MEDIUM_SIGMA_MAX_PIXELS = 2.0;
    public static final double DEFAULT_LARGE_SIGMA_MIN_PIXELS = 2.0;
    public static final double DEFAULT_LARGE_SIGMA_MAX_PIXELS = 2.5;
    public static final double MAXIMUM_SIGMA_PIXELS = 2.5;

    public static final double DEFAULT_SMALL_ALPHA = 9.0;
    public static final double DEFAULT_MEDIUM_ALPHA = 15.0;
    public static final double DEFAULT_LARGE_ALPHA = 6.0;

    public static final double DEFAULT_SMALL_PROPORTION_MIN = 0.20;
    public static final double DEFAULT_SMALL_PROPORTION_MAX = 0.45;
    public static final double DEFAULT_MEDIUM_PROPORTION_MIN = 0.35;
    public static final double DEFAULT_MEDIUM_PROPORTION_MAX = 0.60;
    public static final double DEFAULT_LARGE_PROPORTION_MIN = 0.15;
    public static final double DEFAULT_LARGE_PROPORTION_MAX = 0.30;

    private static final double TOLERANCE = 1.0e-12;

    private final Map<PsfSizeClass, NumericRange> sigmaRangesPixels;
    private final Map<PsfSizeClass, Double> dirichletAlpha;
    private final Map<PsfSizeClass, NumericRange> proportionBounds;

    public PsfSizeMixtureConfig(
            Map<PsfSizeClass, NumericRange> sigmaRangesPixels,
            Map<PsfSizeClass, Double> dirichletAlpha,
            Map<PsfSizeClass, NumericRange> proportionBounds
    ) {
        this.sigmaRangesPixels = immutableRanges(
                sigmaRangesPixels,
                "PSF sigma"
        );
        this.dirichletAlpha = immutableAlpha(dirichletAlpha);
        this.proportionBounds = immutableRanges(
                proportionBounds,
                "PSF-size proportion"
        );
        validateSigmaRanges();
        validateProportions();
    }

    public static PsfSizeMixtureConfig defaultConfig() {
        EnumMap<PsfSizeClass, NumericRange> sigmaRanges =
                new EnumMap<PsfSizeClass, NumericRange>(PsfSizeClass.class);
        sigmaRanges.put(PsfSizeClass.SMALL, new NumericRange(
                DEFAULT_SMALL_SIGMA_MIN_PIXELS,
                DEFAULT_SMALL_SIGMA_MAX_PIXELS
        ));
        sigmaRanges.put(PsfSizeClass.MEDIUM, new NumericRange(
                DEFAULT_MEDIUM_SIGMA_MIN_PIXELS,
                DEFAULT_MEDIUM_SIGMA_MAX_PIXELS
        ));
        sigmaRanges.put(PsfSizeClass.LARGE, new NumericRange(
                DEFAULT_LARGE_SIGMA_MIN_PIXELS,
                DEFAULT_LARGE_SIGMA_MAX_PIXELS
        ));

        EnumMap<PsfSizeClass, Double> alpha =
                new EnumMap<PsfSizeClass, Double>(PsfSizeClass.class);
        alpha.put(PsfSizeClass.SMALL, DEFAULT_SMALL_ALPHA);
        alpha.put(PsfSizeClass.MEDIUM, DEFAULT_MEDIUM_ALPHA);
        alpha.put(PsfSizeClass.LARGE, DEFAULT_LARGE_ALPHA);

        EnumMap<PsfSizeClass, NumericRange> bounds =
                new EnumMap<PsfSizeClass, NumericRange>(PsfSizeClass.class);
        bounds.put(PsfSizeClass.SMALL, new NumericRange(
                DEFAULT_SMALL_PROPORTION_MIN,
                DEFAULT_SMALL_PROPORTION_MAX
        ));
        bounds.put(PsfSizeClass.MEDIUM, new NumericRange(
                DEFAULT_MEDIUM_PROPORTION_MIN,
                DEFAULT_MEDIUM_PROPORTION_MAX
        ));
        bounds.put(PsfSizeClass.LARGE, new NumericRange(
                DEFAULT_LARGE_PROPORTION_MIN,
                DEFAULT_LARGE_PROPORTION_MAX
        ));
        return new PsfSizeMixtureConfig(sigmaRanges, alpha, bounds);
    }

    public NumericRange getSigmaRangePixels(PsfSizeClass sizeClass) {
        return requireClass(sigmaRangesPixels, sizeClass);
    }

    public double getDirichletAlpha(PsfSizeClass sizeClass) {
        return requireClass(dirichletAlpha, sizeClass).doubleValue();
    }

    public NumericRange getProportionBounds(PsfSizeClass sizeClass) {
        return requireClass(proportionBounds, sizeClass);
    }

    public Map<PsfSizeClass, NumericRange> getSigmaRangesPixels() {
        return sigmaRangesPixels;
    }

    public Map<PsfSizeClass, Double> getDirichletAlpha() {
        return dirichletAlpha;
    }

    public Map<PsfSizeClass, NumericRange> getProportionBounds() {
        return proportionBounds;
    }

    private Map<PsfSizeClass, NumericRange> immutableRanges(
            Map<PsfSizeClass, NumericRange> source,
            String name
    ) {
        if (source == null) {
            throw new IllegalArgumentException(name + " ranges cannot be null.");
        }
        EnumMap<PsfSizeClass, NumericRange> copy =
                new EnumMap<PsfSizeClass, NumericRange>(PsfSizeClass.class);
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            NumericRange range = source.get(sizeClass);
            if (range == null) {
                throw new IllegalArgumentException(
                        name + " must define " + sizeClass.name() + "."
                );
            }
            copy.put(sizeClass, range);
        }
        if (source.size() != PsfSizeClass.values().length) {
            throw new IllegalArgumentException(
                    name + " contains an unsupported size class."
            );
        }
        return Collections.unmodifiableMap(copy);
    }

    private Map<PsfSizeClass, Double> immutableAlpha(
            Map<PsfSizeClass, Double> source
    ) {
        if (source == null) {
            throw new IllegalArgumentException("Dirichlet alpha cannot be null.");
        }
        EnumMap<PsfSizeClass, Double> copy =
                new EnumMap<PsfSizeClass, Double>(PsfSizeClass.class);
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            Double value = source.get(sizeClass);
            if (value == null || !isFinite(value.doubleValue())
                    || value.doubleValue() <= 0.0) {
                throw new IllegalArgumentException(
                        "Dirichlet alpha must be finite and positive for "
                                + sizeClass.name() + "."
                );
            }
            copy.put(sizeClass, value);
        }
        if (source.size() != PsfSizeClass.values().length) {
            throw new IllegalArgumentException(
                    "Dirichlet alpha contains an unsupported size class."
            );
        }
        return Collections.unmodifiableMap(copy);
    }

    private void validateProportions() {
        double minimumSum = 0.0;
        double maximumSum = 0.0;
        double alphaSum = 0.0;
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            NumericRange bounds = proportionBounds.get(sizeClass);
            if (bounds.getMinimum() < 0.0 || bounds.getMaximum() > 1.0) {
                throw new IllegalArgumentException(
                        "PSF-size proportion bounds must lie in [0, 1]."
                );
            }
            minimumSum += bounds.getMinimum();
            maximumSum += bounds.getMaximum();
            alphaSum += dirichletAlpha.get(sizeClass).doubleValue();
        }
        if (minimumSum > 1.0 + TOLERANCE
                || maximumSum < 1.0 - TOLERANCE) {
            throw new IllegalArgumentException(
                    "PSF-size proportion bounds cannot contain a unit-sum composition."
            );
        }
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            double priorMean = dirichletAlpha.get(sizeClass).doubleValue()
                    / alphaSum;
            NumericRange bounds = proportionBounds.get(sizeClass);
            if (priorMean < bounds.getMinimum() - TOLERANCE
                    || priorMean > bounds.getMaximum() + TOLERANCE) {
                throw new IllegalArgumentException(
                        "Dirichlet prior mean must satisfy the hard bounds for "
                                + sizeClass.name() + "."
                );
            }
        }
    }

    private void validateSigmaRanges() {
        double previousMaximum = 0.0;
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            NumericRange range = sigmaRangesPixels.get(sizeClass);
            if (range.getMinimum() <= 0.0) {
                throw new IllegalArgumentException(
                        "PSF sigma ranges must be strictly positive."
                );
            }
            if (range.getMinimum() < previousMaximum) {
                throw new IllegalArgumentException(
                        "PSF size-class sigma ranges must be ordered and non-overlapping."
                );
            }
            if (range.getMaximum() > MAXIMUM_SIGMA_PIXELS) {
                throw new IllegalArgumentException(
                        "PSF sigma cannot exceed "
                                + MAXIMUM_SIGMA_PIXELS + " pixels."
                );
            }
            previousMaximum = range.getMaximum();
        }
    }

    private <T> T requireClass(
            Map<PsfSizeClass, T> values,
            PsfSizeClass sizeClass
    ) {
        if (sizeClass == null) {
            throw new IllegalArgumentException("PSF size class cannot be null.");
        }
        return values.get(sizeClass);
    }

    /** Immutable inclusive range used by the mixture configuration. */
    public static final class NumericRange {

        private final double minimum;
        private final double maximum;

        public NumericRange(double minimum, double maximum) {
            if (!isFinite(minimum) || !isFinite(maximum)
                    || minimum < 0.0 || minimum > maximum) {
                throw new IllegalArgumentException(
                        "Range values must be finite, non-negative, and ordered."
                );
            }
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public double getMinimum() {
            return minimum;
        }

        public double getMaximum() {
            return maximum;
        }
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
