package io.github.zhengfangfang0304.synspt.motion.fbm;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.Random;

/**
 * Exact fractional Gaussian-noise sampler based on circulant embedding.
 */
public class DaviesHarteGenerator {

    private static final double EIGENVALUE_RELATIVE_TOLERANCE = 1.0e-12;

    public double[] generate(int length, double hurst, Random random) {
        validateArguments(length, hurst, random);
        if (length == 0) {
            return new double[0];
        }

        int transformLength = embeddingLength(length);
        double[] circulantFirstRow = new double[transformLength];
        circulantFirstRow[0] = autocovariance(0, hurst);
        for (int lag = 1; lag < length; lag++) {
            double covariance = autocovariance(lag, hurst);
            circulantFirstRow[lag] = covariance;
            circulantFirstRow[transformLength - lag] = covariance;
        }

        Complex[] diagonal;
        try {
            diagonal = transformer().transform(
                    circulantFirstRow,
                    TransformType.FORWARD
            );
        } catch (RuntimeException exception) {
            throw new DaviesHarteException(
                    "Unable to diagonalize the FGN circulant embedding.",
                    exception
            );
        }

        double[] eigenvalues = validatedEigenvalues(diagonal);
        Complex[] gaussianSpectrum = sampleSpectrum(eigenvalues, random);
        Complex[] sampled = transformer().transform(
                gaussianSpectrum,
                TransformType.FORWARD
        );
        double[] noise = new double[length];
        for (int index = 0; index < length; index++) {
            noise[index] = sampled[index].getReal();
        }
        return noise;
    }

    /** Unit-time FGN autocovariance at a non-negative integer lag. */
    public double autocovariance(int lag, double hurst) {
        if (lag < 0) {
            throw new IllegalArgumentException("Lag cannot be negative.");
        }
        validateHurst(hurst);
        double exponent = 2.0 * hurst;
        return 0.5 * (
                Math.pow(Math.abs(lag - 1.0), exponent)
                        - 2.0 * Math.pow(lag, exponent)
                        + Math.pow(lag + 1.0, exponent)
        );
    }

    private double[] validatedEigenvalues(Complex[] diagonal) {
        double largestMagnitude = 1.0;
        for (Complex value : diagonal) {
            if (Double.isNaN(value.getReal()) || Double.isInfinite(value.getReal())) {
                throw new DaviesHarteException("Circulant eigenvalue is not finite.");
            }
            largestMagnitude = Math.max(largestMagnitude, Math.abs(value.getReal()));
        }

        double tolerance = EIGENVALUE_RELATIVE_TOLERANCE * largestMagnitude;
        double[] eigenvalues = new double[diagonal.length];
        for (int index = 0; index < diagonal.length; index++) {
            double eigenvalue = diagonal[index].getReal();
            if (eigenvalue < -tolerance) {
                throw new DaviesHarteException(
                        "Circulant embedding has a negative eigenvalue."
                );
            }
            eigenvalues[index] = Math.max(0.0, eigenvalue);
        }
        return eigenvalues;
    }

    private Complex[] sampleSpectrum(double[] eigenvalues, Random random) {
        int length = eigenvalues.length;
        int half = length / 2;
        Complex[] spectrum = new Complex[length];

        spectrum[0] = new Complex(
                Math.sqrt(eigenvalues[0] / length) * random.nextGaussian(),
                0.0
        );
        spectrum[half] = new Complex(
                Math.sqrt(eigenvalues[half] / length) * random.nextGaussian(),
                0.0
        );

        for (int frequency = 1; frequency < half; frequency++) {
            double scale = Math.sqrt(
                    eigenvalues[frequency] / (2.0 * length)
            );
            double real = scale * random.nextGaussian();
            double imaginary = scale * random.nextGaussian();
            spectrum[frequency] = new Complex(real, imaginary);
            spectrum[length - frequency] = new Complex(real, -imaginary);
        }
        return spectrum;
    }

    private int embeddingLength(int noiseLength) {
        long minimum = Math.max(2L, 2L * noiseLength);
        int result = 1;
        while (result < minimum) {
            if (result > (1 << 29)) {
                throw new IllegalArgumentException(
                        "Noise length is too large for a radix-two embedding."
                );
            }
            result <<= 1;
        }
        return result;
    }

    private FastFourierTransformer transformer() {
        return new FastFourierTransformer(DftNormalization.STANDARD);
    }

    private void validateArguments(int length, double hurst, Random random) {
        if (length < 0) {
            throw new IllegalArgumentException("Noise length cannot be negative.");
        }
        validateHurst(hurst);
        if (random == null) {
            throw new IllegalArgumentException("Random source cannot be null.");
        }
    }

    private void validateHurst(double hurst) {
        if (Double.isNaN(hurst)
                || Double.isInfinite(hurst)
                || hurst <= 0.0
                || hurst >= 1.0) {
            throw new IllegalArgumentException("Hurst exponent must be in (0, 1).");
        }
    }
}
