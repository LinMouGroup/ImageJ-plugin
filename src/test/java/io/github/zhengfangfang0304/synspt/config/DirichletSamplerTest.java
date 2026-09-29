package io.github.zhengfangfang0304.synspt.config;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DirichletSamplerTest {

    @Test
    public void outputIsPositiveNormalizedAndSeedReproducible() {
        DirichletSampler sampler = new DirichletSampler();
        double[] alpha = new double[] {0.5, 2.0, 5.5};
        double[] first = sampler.sample(alpha, new Random(42L));
        double[] repeated = sampler.sample(alpha, new Random(42L));
        double sum = 0.0;
        for (double value : first) {
            assertTrue(value > 0.0);
            sum += value;
        }
        assertEquals(1.0, sum, 0.0);
        assertArrayEquals(first, repeated, 0.0);
    }
}
