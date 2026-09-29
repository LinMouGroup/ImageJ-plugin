package io.github.zhengfangfang0304.synspt.optical;

import org.apache.commons.math3.special.Erf;

/** High-precision pixel-integrated circular Gaussian PSF renderer. */
public final class CircularGaussianPsfRenderer implements PsfKernelRenderer {

    private static final double SQRT_TWO = Math.sqrt(2.0);

    @Override
    public PsfShapeType getShapeType() {
        return PsfShapeType.CIRCULAR_GAUSSIAN;
    }

    @Override
    public void render(
            double[] expectedPhotons,
            int width,
            int height,
            double centerX,
            double centerY,
            ParticleImagingParameters imagingParameters
    ) {
        validate(expectedPhotons, width, height, imagingParameters);
        ParticlePsfParameters psf = imagingParameters.getPsfParameters();
        if (psf.getShapeType() != getShapeType()) {
            throw new IllegalArgumentException(
                    "Circular renderer requires a circular Gaussian PSF."
            );
        }
        double radius = psf.getRadiusPixels();
        int minimumX = Math.max(0, (int) Math.floor(centerX - radius));
        int maximumX = Math.min(width - 1, (int) Math.ceil(centerX + radius) - 1);
        int minimumY = Math.max(0, (int) Math.floor(centerY - radius));
        int maximumY = Math.min(height - 1, (int) Math.ceil(centerY + radius) - 1);
        double sigma = psf.getSigmaXPixels();

        for (int y = minimumY; y <= maximumY; y++) {
            double weightY = integratedGaussianPixel(y, centerY, sigma);
            int rowOffset = y * width;
            for (int x = minimumX; x <= maximumX; x++) {
                double weightX = integratedGaussianPixel(x, centerX, sigma);
                addChecked(
                        expectedPhotons,
                        rowOffset + x,
                        imagingParameters.getEmitterPhotonsPerFrame()
                                * weightX * weightY
                );
            }
        }
    }

    private double integratedGaussianPixel(int pixel, double center, double sigma) {
        double scale = SQRT_TWO * sigma;
        double upper = (pixel + 1.0 - center) / scale;
        double lower = (pixel - center) / scale;
        return 0.5 * (Erf.erf(upper) - Erf.erf(lower));
    }

    static void validate(
            double[] expectedPhotons,
            int width,
            int height,
            ParticleImagingParameters imagingParameters
    ) {
        if (expectedPhotons == null
                || imagingParameters == null
                || width <= 0
                || height <= 0
                || expectedPhotons.length != width * height) {
            throw new IllegalArgumentException("Invalid PSF rendering arguments.");
        }
    }

    static void addChecked(double[] expectedPhotons, int index, double value) {
        expectedPhotons[index] += value;
        if (Double.isInfinite(expectedPhotons[index])) {
            throw new IllegalArgumentException(
                    "Expected photon count overflowed at one pixel."
            );
        }
    }
}
