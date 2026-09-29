package io.github.zhengfangfang0304.synspt.noise;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.optical.ExpectedPhotonFrame;

import java.util.Random;

/** Poisson photon shot noise followed by Gaussian camera read noise. */
public final class CameraNoiseModel implements NoiseModel {

    private final PoissonSampler poissonSampler;

    public CameraNoiseModel() {
        this(new PoissonSampler());
    }

    public CameraNoiseModel(PoissonSampler poissonSampler) {
        if (poissonSampler == null) {
            throw new IllegalArgumentException("Poisson sampler cannot be null.");
        }
        this.poissonSampler = poissonSampler;
    }

    @Override
    public CameraFrame apply(
            ExpectedPhotonFrame expectedPhotonFrame,
            MicroscopeConfig microscopeConfig,
            long randomSeed
    ) {
        if (expectedPhotonFrame == null
                || microscopeConfig == null) {
            throw new IllegalArgumentException(
                    "Expected photon frame and microscope config cannot be null."
            );
        }

        Random random = new Random(randomSeed);
        short[] output = new short[expectedPhotonFrame.getPixelCount()];
        int maximumAdu = (1 << microscopeConfig.getBitDepth()) - 1;
        double gain = microscopeConfig.getGainAduPerPhoton();
        double offset = microscopeConfig.getOffsetAdu();
        double gaussianNoiseSigma = microscopeConfig.getReadNoiseSigmaAdu();

        for (int pixelIndex = 0; pixelIndex < output.length; pixelIndex++) {
            long detectedPhotons = poissonSampler.sample(
                    expectedPhotonFrame.getExpectedPhotons(pixelIndex),
                    random
            );
            double gaussianNoise = gaussianNoiseSigma == 0.0
                    ? 0.0
                    : gaussianNoiseSigma * random.nextGaussian();
            double analogueAdu = offset + gain * detectedPhotons + gaussianNoise;
            int digitizedAdu = digitize(analogueAdu, maximumAdu);
            output[pixelIndex] = (short) digitizedAdu;
        }
        return CameraFrame.takeOwnership(
                expectedPhotonFrame.getWidth(),
                expectedPhotonFrame.getHeight(),
                microscopeConfig.getBitDepth(),
                output
        );
    }

    private int digitize(double analogueAdu, int maximumAdu) {
        if (analogueAdu <= 0.0) {
            return 0;
        }
        if (analogueAdu >= maximumAdu) {
            return maximumAdu;
        }
        return (int) Math.floor(analogueAdu + 0.5);
    }
}
