package io.github.zhengfangfang0304.particletracking.simulation.rendering;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.FloatProcessor;
import io.github.zhengfangfang0304.particletracking.simulation.config.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticParticle;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;

/**
 * Renders particle positions with a two-dimensional Gaussian PSF.
 */
public class GaussianSpotRenderer {

    private static final double SPOT_AMPLITUDE = 200.0;

    public ImagePlus render(
            List<SyntheticParticle> particles,
            SimulationConfig config
    ) {
        ImageStack stack = new ImageStack(config.width, config.height);
        Random random = new Random();

        for (int frame = 1; frame <= config.frames; frame++) {
            float[] pixels = createNoisyBackground(
                    config.width * config.height,
                    config.noiseSigma,
                    random
            );

            for (SyntheticParticle particle : particles) {
                Point2D.Double position =
                        particle.getTrajectory().get(frame);

                if (position != null) {
                    drawGaussianSpot(
                            pixels,
                            config.width,
                            config.height,
                            position.x,
                            position.y,
                            config.psfSigma
                    );
                }
            }

            stack.addSlice(
                    "Frame " + frame,
                    new FloatProcessor(
                            config.width,
                            config.height,
                            pixels
                    )
            );
        }

        ImagePlus image = new ImagePlus("Synthetic SPT Data", stack);
        image.setDimensions(1, 1, config.frames);
        image.setOpenAsHyperStack(true);
        image.setDisplayRange(0.0, 255.0);
        applyCalibration(image, config);
        return image;
    }

    private void applyCalibration(
            ImagePlus image,
            SimulationConfig config
    ) {
        Calibration calibration = image.getCalibration();
        calibration.pixelWidth =
                config.getImagingConfig()
                        .getPixelSizeUmPerPixel();
        calibration.pixelHeight =
                config.getImagingConfig()
                        .getPixelSizeUmPerPixel();
        calibration.setUnit("um");
        calibration.frameInterval =
                config.getImagingConfig()
                        .getFrameIntervalSeconds();
        calibration.setTimeUnit("s");
    }

    private float[] createNoisyBackground(
            int pixelCount,
            double noiseSigma,
            Random random
    ) {
        float[] pixels = new float[pixelCount];

        for (int i = 0; i < pixelCount; i++) {
            pixels[i] = (float) Math.max(
                    0.0,
                    random.nextGaussian() * noiseSigma
            );
        }

        return pixels;
    }

    private void drawGaussianSpot(
            float[] pixels,
            int width,
            int height,
            double centerX,
            double centerY,
            double sigma
    ) {
        int radius = (int) Math.ceil(3.0 * sigma);
        int xMin = Math.max(0, (int) Math.floor(centerX - radius));
        int xMax = Math.min(width - 1, (int) Math.ceil(centerX + radius));
        int yMin = Math.max(0, (int) Math.floor(centerY - radius));
        int yMax = Math.min(height - 1, (int) Math.ceil(centerY + radius));
        double twoSigmaSquared = 2.0 * sigma * sigma;

        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                double dx = x - centerX;
                double dy = y - centerY;
                double intensity = SPOT_AMPLITUDE * Math.exp(
                        -(dx * dx + dy * dy) / twoSigmaSquared
                );

                pixels[y * width + x] += (float) intensity;
            }
        }
    }
}
