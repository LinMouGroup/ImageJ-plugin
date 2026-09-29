package io.github.zhengfangfang0304.synspt.motion.fbm;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FractionalGaussianNoiseTest {

    private static final int STATISTICAL_SAMPLE_SIZE = 16384;

    @Test
    public void hurstOneHalfReducesToIndependentUnitGaussianNoise() {
        double[] increments = new FractionalGaussianNoise(0.5).generate(
                STATISTICAL_SAMPLE_SIZE,
                new Random(2L)
        );

        assertTrue(sampleVariance(increments) > 0.90);
        assertTrue(sampleVariance(increments) < 1.10);
        assertTrue(Math.abs(lagOneCorrelation(increments)) < 0.06);
    }

    @Test
    public void persistentFgnHasPositiveIncrementCorrelation() {
        double[] increments = new FractionalGaussianNoise(0.75).generate(
                STATISTICAL_SAMPLE_SIZE,
                new Random(3L)
        );
        assertTrue(lagOneCorrelation(increments) > 0.20);
    }

    @Test
    public void antipersistentFgnHasNegativeIncrementCorrelation() {
        double[] increments = new FractionalGaussianNoise(0.25).generate(
                STATISTICAL_SAMPLE_SIZE,
                new Random(4L)
        );
        assertTrue(lagOneCorrelation(increments) < -0.15);
    }

    @Test
    public void invalidDaviesHarteEmbeddingFallsBackToHosking() {
        DaviesHarteGenerator failing = new DaviesHarteGenerator() {
            @Override
            public double[] generate(int length, double hurst, Random random) {
                throw new DaviesHarteException("Forced failure for fallback test.");
            }
        };
        FractionalGaussianNoise noise = new FractionalGaussianNoise(
                0.70,
                failing,
                new HoskingGenerator()
        );

        double[] values = noise.generate(64, new Random(5L));

        assertEquals(64, values.length);
        assertTrue(noise.wasHoskingFallbackUsed());
    }

    @Test
    public void autocovarianceMatchesTheAnalyticalLagValues() {
        DaviesHarteGenerator generator = new DaviesHarteGenerator();
        assertEquals(1.0, generator.autocovariance(0, 0.5), 1.0e-12);
        assertEquals(0.0, generator.autocovariance(1, 0.5), 1.0e-12);
        assertTrue(generator.autocovariance(1, 0.75) > 0.0);
        assertTrue(generator.autocovariance(1, 0.25) < 0.0);
    }

    private double sampleVariance(double[] values) {
        double mean = mean(values);
        double sum = 0.0;
        for (double value : values) {
            double centered = value - mean;
            sum += centered * centered;
        }
        return sum / (values.length - 1);
    }

    private double lagOneCorrelation(double[] values) {
        double mean = mean(values);
        double covariance = 0.0;
        double variance = 0.0;
        for (int index = 0; index < values.length; index++) {
            double centered = values[index] - mean;
            variance += centered * centered;
            if (index > 0) {
                covariance += centered * (values[index - 1] - mean);
            }
        }
        return covariance / variance;
    }

    private double mean(double[] values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }
}
