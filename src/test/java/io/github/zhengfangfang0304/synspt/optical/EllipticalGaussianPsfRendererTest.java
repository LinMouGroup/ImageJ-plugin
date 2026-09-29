package io.github.zhengfangfang0304.synspt.optical;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EllipticalGaussianPsfRendererTest {

    @Test
    public void rotatedEllipticalEmitterConservesPhotonsAndHasOrientation() {
        int width = 101;
        double[] pixels = new double[width * width];
        ParticleImagingParameters imaging = new ParticleImagingParameters(
                new ParticlePsfParameters(
                        PsfSizeClass.MEDIUM,
                        PsfShapeType.ELLIPTICAL_GAUSSIAN,
                        12.5,
                        2.5,
                        1.0,
                        Math.PI / 4.0
                ),
                1000.0
        );
        new EllipticalGaussianPsfRenderer().render(
                pixels, width, width, 50.5, 50.5, imaging
        );

        assertEquals(1000.0, sum(pixels), 0.5);
        assertTrue(pixels[54 * width + 54] > pixels[46 * width + 54]);
    }

    private double sum(double[] values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }
}
