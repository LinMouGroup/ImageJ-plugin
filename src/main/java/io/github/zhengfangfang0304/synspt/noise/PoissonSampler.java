package io.github.zhengfangfang0304.synspt.noise;

import org.apache.commons.math3.special.Gamma;

import java.util.Random;

/** Poisson sampler using inversion for small means and PTRS for large means. */
public final class PoissonSampler {

    private static final double INVERSION_THRESHOLD = 30.0;
    private static final double NORMAL_APPROXIMATION_THRESHOLD = 1.0e8;

    public long sample(double mean, Random random) {
        if (Double.isNaN(mean) || Double.isInfinite(mean) || mean < 0.0) {
            throw new IllegalArgumentException(
                    "Poisson mean must be finite and non-negative."
            );
        }
        if (random == null) {
            throw new IllegalArgumentException("Random source cannot be null.");
        }
        if (mean == 0.0) {
            return 0L;
        }
        if (mean < INVERSION_THRESHOLD) {
            return sampleByInversion(mean, random);
        }
        if (mean >= NORMAL_APPROXIMATION_THRESHOLD) {
            return sampleByAsymptoticNormal(mean, random);
        }
        return sampleByTransformedRejection(mean, random);
    }

    private long sampleByInversion(double mean, Random random) {
        double limit = Math.exp(-mean);
        double product = 1.0;
        long count = 0L;
        do {
            count++;
            product *= random.nextDouble();
        } while (product > limit);
        return count - 1L;
    }

    /** Hörmann's transformed rejection method (PTRS). */
    private long sampleByTransformedRejection(double mean, Random random) {
        double squareRootMean = Math.sqrt(mean);
        double logMean = Math.log(mean);
        double b = 0.931 + 2.53 * squareRootMean;
        double a = -0.059 + 0.02483 * b;
        double inverseAlpha = 1.1239 + 1.1328 / (b - 3.4);
        double squeeze = 0.9277 - 3.6224 / (b - 2.0);

        while (true) {
            double centeredUniform = random.nextDouble() - 0.5;
            double acceptanceUniform = random.nextDouble();
            double distanceFromEdge = 0.5 - Math.abs(centeredUniform);
            double candidateValue = Math.floor(
                    (2.0 * a / distanceFromEdge + b) * centeredUniform
                            + mean + 0.43
            );
            if (candidateValue < 0.0) {
                continue;
            }
            long candidate = (long) candidateValue;
            if (distanceFromEdge >= 0.07 && acceptanceUniform <= squeeze) {
                return candidate;
            }
            if (distanceFromEdge < 0.013
                    && acceptanceUniform > distanceFromEdge) {
                continue;
            }

            double logAcceptance = Math.log(
                    acceptanceUniform * inverseAlpha
                            / (a / (distanceFromEdge * distanceFromEdge) + b)
            );
            double logProbability = -mean
                    + candidate * logMean
                    - Gamma.logGamma(candidate + 1.0);
            if (logAcceptance <= logProbability) {
                return candidate;
            }
        }
    }

    private long sampleByAsymptoticNormal(double mean, Random random) {
        double sample = mean + Math.sqrt(mean) * random.nextGaussian();
        if (sample <= 0.0) {
            return 0L;
        }
        if (sample >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.round(sample);
    }
}
