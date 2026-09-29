package io.github.zhengfangfang0304.synspt.noise;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.optical.ExpectedPhotonFrame;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CameraNoiseModelTest {

    @Test
    public void zeroPhotonAndZeroReadNoiseProducesExactCameraOffset() {
        ExpectedPhotonFrame expected = new ExpectedPhotonFrame(
                4,
                1,
                new double[] {0.0, 0.0, 0.0, 0.0}
        );
        CameraFrame frame = new CameraNoiseModel().apply(
                expected,
                microscope(0.0, 1.0, 100.0, 16),
                11L
        );

        for (int x = 0; x < 4; x++) {
            assertEquals(100, frame.getUnsignedAdu(x, 0));
        }
    }

    @Test
    public void photonGainAndShotNoiseHaveExpectedMeanAndVariance() {
        int samples = 100000;
        double expectedPhotons = 20.0;
        double gain = 2.0;
        double offset = 100.0;
        double[] photons = new double[samples];
        Arrays.fill(photons, expectedPhotons);
        CameraFrame frame = new CameraNoiseModel().apply(
                new ExpectedPhotonFrame(samples, 1, photons),
                microscope(0.0, gain, offset, 16),
                22L
        );

        double[] statistics = meanAndVariance(frame);
        assertEquals(offset + gain * expectedPhotons, statistics[0], 0.3);
        assertEquals(gain * gain * expectedPhotons, statistics[1], 2.0);
    }

    @Test
    public void backgroundVarianceContainsOnlyPoissonAndReadNoise() {
        int samples = 100000;
        double[] photons = new double[samples];
        Arrays.fill(photons, 20.0);
        CameraFrame frame = new CameraNoiseModel().apply(
                new ExpectedPhotonFrame(samples, 1, photons),
                microscope(3.0, 1.0, 100.0, 16),
                23L
        );
        double[] statistics = meanAndVariance(frame);
        assertEquals(120.0, statistics[0], 0.2);
        assertEquals(20.0 + 9.0, statistics[1], 1.0);
    }

    @Test
    public void readNoiseIsGaussianAndSeedReproducible() {
        int samples = 100000;
        ExpectedPhotonFrame expected = new ExpectedPhotonFrame(
                samples,
                1,
                new double[samples]
        );
        MicroscopeConfig config = microscope(3.0, 1.0, 100.0, 16);
        CameraNoiseModel noiseModel = new CameraNoiseModel();
        CameraFrame first = noiseModel.apply(expected, config, 33L);
        CameraFrame second = noiseModel.apply(expected, config, 33L);
        double[] statistics = meanAndVariance(first);

        assertArrayEquals(first.copyPixels(), second.copyPixels());
        assertEquals(100.0, statistics[0], 0.1);
        assertEquals(9.0, statistics[1], 0.5);
    }

    @Test
    public void digitizerSaturatesAtConfiguredBitDepth() {
        CameraFrame frame = new CameraNoiseModel().apply(
                new ExpectedPhotonFrame(1, 1, new double[] {1.0e9}),
                microscope(0.0, 1.0, 0.0, 8),
                44L
        );
        assertEquals(255, frame.getUnsignedAdu(0, 0));
    }

    private MicroscopeConfig microscope(
            double readNoiseSigma,
            double gain,
            double offset,
            int bitDepth
    ) {
        return new MicroscopeConfig(
                0.1,
                0.01,
                0.0,
                readNoiseSigma,
                gain,
                offset,
                bitDepth
        );
    }

    private double[] meanAndVariance(CameraFrame frame) {
        double sum = 0.0;
        double squaredSum = 0.0;
        for (int x = 0; x < frame.getWidth(); x++) {
            double value = frame.getUnsignedAdu(x, 0);
            sum += value;
            squaredSum += value * value;
        }
        double mean = sum / frame.getWidth();
        double variance = (squaredSum - frame.getWidth() * mean * mean)
                / (frame.getWidth() - 1);
        return new double[] {mean, variance};
    }
}
