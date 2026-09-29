package io.github.zhengfangfang0304.synspt.config;

import java.util.Random;

/** Dependency-free gamma-normalization sampler for Dirichlet variates. */
public final class DirichletSampler {

    public double[] sample(double[] alpha, Random random) {
        if (alpha == null || alpha.length == 0 || random == null) {
            throw new IllegalArgumentException(
                    "Dirichlet alpha and random source cannot be null or empty."
            );
        }
        double[] gamma = new double[alpha.length];
        double sum = 0.0;
        for (int index = 0; index < alpha.length; index++) {
            double shape = alpha[index];
            if (Double.isNaN(shape)
                    || Double.isInfinite(shape)
                    || shape <= 0.0) {
                throw new IllegalArgumentException(
                        "Every Dirichlet alpha must be finite and positive."
                );
            }
            gamma[index] = Math.max(Double.MIN_VALUE, sampleGamma(shape, random));
            sum += gamma[index];
        }
        if (Double.isInfinite(sum) || sum <= 0.0) {
            throw new IllegalStateException("Dirichlet normalization failed.");
        }
        double normalizedSum = 0.0;
        for (int index = 0; index < gamma.length - 1; index++) {
            gamma[index] /= sum;
            normalizedSum += gamma[index];
        }
        gamma[gamma.length - 1] = Math.max(
                Double.MIN_VALUE,
                1.0 - normalizedSum
        );
        double correctedSum = 0.0;
        for (double value : gamma) {
            correctedSum += value;
        }
        gamma[gamma.length - 1] += 1.0 - correctedSum;
        return gamma;
    }

    private double sampleGamma(double shape, Random random) {
        if (shape < 1.0) {
            double uniformOpen = 1.0 - random.nextDouble();
            return sampleGamma(shape + 1.0, random)
                    * Math.pow(uniformOpen, 1.0 / shape);
        }
        double d = shape - 1.0 / 3.0;
        double c = 1.0 / Math.sqrt(9.0 * d);
        while (true) {
            double gaussian = random.nextGaussian();
            double transformed = 1.0 + c * gaussian;
            if (transformed <= 0.0) {
                continue;
            }
            double cube = transformed * transformed * transformed;
            double uniform = random.nextDouble();
            if (uniform < 1.0 - 0.0331 * gaussian * gaussian
                    * gaussian * gaussian
                    || Math.log(uniform) < 0.5 * gaussian * gaussian
                    + d * (1.0 - cube + Math.log(cube))) {
                return d * cube;
            }
        }
    }
}
