package io.github.zhengfangfang0304.synspt.noise;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PoissonSamplerTest {

    @Test
    public void smallAndLargeMeansHavePoissonMeanAndVariance() {
        assertPoissonStatistics(4.0, 100000, 101L, 0.05, 0.12);
        assertPoissonStatistics(100.0, 100000, 202L, 0.20, 2.0);
    }

    @Test
    public void zeroMeanAndSeededSequencesAreDeterministic() {
        PoissonSampler sampler = new PoissonSampler();
        assertEquals(0L, sampler.sample(0.0, new Random(1L)));

        long[] first = sequence(sampler, 35.0, 100, new Random(303L));
        long[] second = sequence(sampler, 35.0, 100, new Random(303L));
        assertArrayEquals(first, second);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeMeans() {
        new PoissonSampler().sample(-1.0, new Random(1L));
    }

    private void assertPoissonStatistics(
            double expectedMean,
            int samples,
            long seed,
            double meanTolerance,
            double varianceTolerance
    ) {
        PoissonSampler sampler = new PoissonSampler();
        Random random = new Random(seed);
        double sum = 0.0;
        double squaredSum = 0.0;
        for (int index = 0; index < samples; index++) {
            long value = sampler.sample(expectedMean, random);
            sum += value;
            squaredSum += (double) value * value;
        }
        double sampleMean = sum / samples;
        double sampleVariance = (squaredSum - samples * sampleMean * sampleMean)
                / (samples - 1);
        assertEquals(expectedMean, sampleMean, meanTolerance);
        assertEquals(expectedMean, sampleVariance, varianceTolerance);
    }

    private long[] sequence(
            PoissonSampler sampler,
            double mean,
            int length,
            Random random
    ) {
        long[] values = new long[length];
        for (int index = 0; index < length; index++) {
            values[index] = sampler.sample(mean, random);
        }
        return values;
    }
}
