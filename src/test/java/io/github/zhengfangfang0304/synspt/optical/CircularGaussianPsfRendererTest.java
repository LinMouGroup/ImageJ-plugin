package io.github.zhengfangfang0304.synspt.optical;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CircularGaussianPsfRendererTest {

    @Test
    public void centeredEmitterConservesPhotonsWithPixelIntegration() {
        double[] pixels = new double[101 * 101];
        new CircularGaussianPsfRenderer().render(
                pixels, 101, 101, 50.5, 50.5, circular(1.5, 1000.0)
        );
        assertEquals(1000.0, sum(pixels), 0.01);
        assertTrue(pixels[50 * 101 + 50] > pixels[50 * 101 + 49]);
        assertEquals(pixels[50 * 101 + 49], pixels[50 * 101 + 51], 1.0e-12);
    }

    @Test
    public void detectorEdgeCropsHalfOfCircularEmitterPhotons() {
        double[] pixels = new double[101 * 101];
        new CircularGaussianPsfRenderer().render(
                pixels, 101, 101, 0.0, 50.5, circular(1.5, 1000.0)
        );
        assertEquals(500.0, sum(pixels), 0.02);
    }

    private ParticleImagingParameters circular(double sigma, double photons) {
        return new ParticleImagingParameters(
                new ParticlePsfParameters(
                        PsfSizeClass.MEDIUM,
                        PsfShapeType.CIRCULAR_GAUSSIAN,
                        5.0 * sigma,
                        sigma,
                        sigma,
                        0.0
                ),
                photons
        );
    }

    private double sum(double[] values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }
}
