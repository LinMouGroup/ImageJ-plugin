package io.github.zhengfangfang0304.synspt.optical;

/** Supersampled pixel integration for a rotated elliptical Gaussian PSF. */
public final class EllipticalGaussianPsfRenderer implements PsfKernelRenderer {

    private static final int SUBPIXEL_SAMPLES = 4;
    private static final double TWO_PI = 2.0 * Math.PI;

    @Override
    public PsfShapeType getShapeType() {
        return PsfShapeType.ELLIPTICAL_GAUSSIAN;
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
        CircularGaussianPsfRenderer.validate(
                expectedPhotons,
                width,
                height,
                imagingParameters
        );
        ParticlePsfParameters psf = imagingParameters.getPsfParameters();
        if (psf.getShapeType() != getShapeType()) {
            throw new IllegalArgumentException(
                    "Elliptical renderer requires an elliptical Gaussian PSF."
            );
        }
        double radius = psf.getRadiusPixels();
        int minimumX = Math.max(0, (int) Math.floor(centerX - radius));
        int maximumX = Math.min(width - 1, (int) Math.ceil(centerX + radius) - 1);
        int minimumY = Math.max(0, (int) Math.floor(centerY - radius));
        int maximumY = Math.min(height - 1, (int) Math.ceil(centerY + radius) - 1);
        double cosine = Math.cos(psf.getRotationRadians());
        double sine = Math.sin(psf.getRotationRadians());
        double sigmaX = psf.getSigmaXPixels();
        double sigmaY = psf.getSigmaYPixels();
        double normalization = 1.0 / (TWO_PI * sigmaX * sigmaY);
        double sampleArea = 1.0 / (SUBPIXEL_SAMPLES * SUBPIXEL_SAMPLES);

        for (int y = minimumY; y <= maximumY; y++) {
            int rowOffset = y * width;
            for (int x = minimumX; x <= maximumX; x++) {
                double pixelWeight = 0.0;
                for (int sy = 0; sy < SUBPIXEL_SAMPLES; sy++) {
                    double sampleY = y + (sy + 0.5) / SUBPIXEL_SAMPLES - centerY;
                    for (int sx = 0; sx < SUBPIXEL_SAMPLES; sx++) {
                        double sampleX = x + (sx + 0.5) / SUBPIXEL_SAMPLES - centerX;
                        double rotatedX = cosine * sampleX + sine * sampleY;
                        double rotatedY = -sine * sampleX + cosine * sampleY;
                        double exponent = -0.5 * (
                                rotatedX * rotatedX / (sigmaX * sigmaX)
                                        + rotatedY * rotatedY / (sigmaY * sigmaY)
                        );
                        pixelWeight += normalization * Math.exp(exponent) * sampleArea;
                    }
                }
                CircularGaussianPsfRenderer.addChecked(
                        expectedPhotons,
                        rowOffset + x,
                        imagingParameters.getEmitterPhotonsPerFrame() * pixelWeight
                );
            }
        }
    }
}
