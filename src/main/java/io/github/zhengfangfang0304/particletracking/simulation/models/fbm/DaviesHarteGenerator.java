package io.github.zhengfangfang0304.particletracking.simulation.models.fbm;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.Arrays;
import java.util.Random;

/**
 * Generates fractional Gaussian noise with the Davies-Harte method.
 *
 * <p>The implementation builds a circulant embedding of the fractional
 * Gaussian noise autocovariance, diagonalizes it with the Apache Commons
 * Math FFT, and samples the resulting complex Gaussian spectrum.</p>
 */
public class DaviesHarteGenerator {

    private static final double EIGENVALUE_TOLERANCE = 1.0e-12;

    /**
     * Generates unit-time fractional Gaussian noise.
     *
     * @param length number of increments
     * @param hurst Hurst exponent in {@code (0, 1)}
     * @param random Gaussian random source
     * @return correlated Gaussian increments with unit marginal variance
     * @throws DaviesHarteException when the circulant embedding is invalid
     */
    public double[] generate(
            int length,
            double hurst,
            Random random
    ) {
        validateArguments(length, hurst, random);

        if (length == 0) {
            return new double[0];
        }

        int embeddingSize = embeddingSize(length);
        double[] firstRow = new double[embeddingSize];

        firstRow[0] = autocovariance(0, hurst);
        for (int lag = 1; lag < length; lag++) {
            double covariance = autocovariance(lag, hurst);
            firstRow[lag] = covariance;
            firstRow[embeddingSize - lag] = covariance;
        }

        Complex[] transformed;
        try {
            transformed =
                    new FastFourierTransformer(
                            DftNormalization.STANDARD
                    ).transform(
                            firstRow,
                            TransformType.FORWARD
                    );
        } catch (RuntimeException exception) {
            throw new DaviesHarteException(
                    "Cannot diagonalize the circulant embedding.",
                    exception
            );
        }

        double[] eigenvalues = new double[embeddingSize];
        double largestMagnitude = 1.0;

        for (int i = 0; i < embeddingSize; i++) {
            eigenvalues[i] = transformed[i].getReal();
            largestMagnitude =
                    Math.max(
                            largestMagnitude,
                            Math.abs(eigenvalues[i])
                    );
        }

        double tolerance =
                EIGENVALUE_TOLERANCE * largestMagnitude;
        for (int i = 0; i < eigenvalues.length; i++) {
            if (eigenvalues[i] < -tolerance) {
                throw new DaviesHarteException(
                        "Circulant embedding has a negative eigenvalue."
                );
            }
            eigenvalues[i] = Math.max(0.0, eigenvalues[i]);
        }

        Complex[] spectrum = new Complex[embeddingSize];
        Arrays.fill(spectrum, Complex.ZERO);
        int half = embeddingSize / 2;

        spectrum[0] =
                new Complex(
                        Math.sqrt(
                                eigenvalues[0] / embeddingSize
                        ) * random.nextGaussian(),
                        0.0
                );
        spectrum[half] =
                new Complex(
                        Math.sqrt(
                                eigenvalues[half] / embeddingSize
                        ) * random.nextGaussian(),
                        0.0
                );

        for (int frequency = 1; frequency < half; frequency++) {
            double scale =
                    Math.sqrt(
                            eigenvalues[frequency]
                                    / (2.0 * embeddingSize)
                    );
            double real = scale * random.nextGaussian();
            double imaginary = scale * random.nextGaussian();

            spectrum[frequency] =
                    new Complex(real, imaginary);
            spectrum[embeddingSize - frequency] =
                    new Complex(real, -imaginary);
        }

        Complex[] sampled =
                new FastFourierTransformer(
                        DftNormalization.STANDARD
                ).transform(
                        spectrum,
                        TransformType.FORWARD
                );
        double[] noise = new double[length];

        for (int i = 0; i < length; i++) {
            noise[i] = sampled[i].getReal();
        }

        return noise;
    }

    /**
     * Returns the fractional Gaussian noise autocovariance at one lag.
     */
    public double autocovariance(
            int lag,
            double hurst
    ) {
        if (lag < 0) {
            throw new IllegalArgumentException(
                    "Lag cannot be negative."
            );
        }
        if (!(hurst > 0.0 && hurst < 1.0)) {
            throw new IllegalArgumentException(
                    "Hurst exponent must be between zero and one."
            );
        }

        double exponent = 2.0 * hurst;
        return 0.5
                * (
                Math.pow(Math.abs(lag - 1.0), exponent)
                        - 2.0 * Math.pow(lag, exponent)
                        + Math.pow(lag + 1.0, exponent)
        );
    }

    private void validateArguments(
            int length,
            double hurst,
            Random random
    ) {
        if (length < 0) {
            throw new IllegalArgumentException(
                    "Noise length cannot be negative."
            );
        }
        if (!(hurst > 0.0 && hurst < 1.0)) {
            throw new IllegalArgumentException(
                    "Hurst exponent must be between zero and one."
            );
        }
        if (random == null) {
            throw new IllegalArgumentException(
                    "Random generator cannot be null."
            );
        }
    }

    private int embeddingSize(int length) {
        long minimum = Math.max(2L, 2L * length);
        int size = 1;

        while (size < minimum) {
            if (size > (1 << 29)) {
                throw new IllegalArgumentException(
                        "Noise length is too large for FFT embedding."
                );
            }
            size <<= 1;
        }

        return size;
    }
}
