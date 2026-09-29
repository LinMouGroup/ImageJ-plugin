package io.github.zhengfangfang0304.synspt.noise;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.config.NoiseConfig;
import io.github.zhengfangfang0304.synspt.optical.CircularGaussianPsfRenderer;
import io.github.zhengfangfang0304.synspt.optical.ParticleAppearanceSample;
import io.github.zhengfangfang0304.synspt.optical.ParticleImagingParameters;
import io.github.zhengfangfang0304.synspt.optical.ParticlePsfParameters;
import io.github.zhengfangfang0304.synspt.optical.PsfShapeType;
import io.github.zhengfangfang0304.synspt.optical.PsfSizeClass;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SnrNoiseCalibratorTest {

    @Test
    public void targetTenCalibratesMedianPhotonSnr() {
        SnrNoiseCalibrator calibrator = new SnrNoiseCalibrator();
        PhotonCalibrationResult result = calibrator.calibrate(
                NoiseConfig.defaultConfig(),
                MicroscopeConfig.defaultConfig(),
                samples()
        );
        assertEquals(10.0, result.getTargetSnr(), 0.0);
        assertEquals(10.0, result.getPredictedSnr(), 1.0e-10);
        assertEquals(10.0, result.getRealizedSnr(), 1.0e-10);
        assertEquals("photon_budget", result.getCalibrationMethod());
        assertEquals(2.0, result.getPhysicalReadNoiseSigmaAdu(), 0.0);
        assertEquals(20.0, result.getBackgroundPhotonsPerPixel(), 0.0);
        assertEquals(1.0,
                result.getRelativeBrightnesses().get(1).doubleValue(), 0.0);
        assertEquals(result.getPhotonScale(),
                result.getReferencePhotonBudget(), 1.0e-10);
    }

    @Test
    public void photonBudgetIncreasesWithTargetSnr() {
        SnrNoiseCalibrator calibrator = new SnrNoiseCalibrator();
        double scale5 = calibrator.calibrate(
                noise(5.0), MicroscopeConfig.defaultConfig(), samples()
        ).getPhotonScale();
        double scale10 = calibrator.calibrate(
                noise(10.0), MicroscopeConfig.defaultConfig(), samples()
        ).getPhotonScale();
        double scale20 = calibrator.calibrate(
                noise(20.0), MicroscopeConfig.defaultConfig(), samples()
        ).getPhotonScale();
        assertTrue(scale5 < scale10);
        assertTrue(scale10 < scale20);
    }

    @Test
    public void singleParticleBudgetMatchesPeakPixelInverseFormula() {
        ParticleAppearanceSample appearance = sample(1000.0, 1.5);
        List<ParticleAppearanceSample> appearances =
                new ArrayList<ParticleAppearanceSample>();
        appearances.add(appearance);
        PhotonCalibrationResult result = new SnrNoiseCalibrator().calibrate(
                noise(10.0),
                MicroscopeConfig.defaultConfig(),
                appearances
        );

        double targetSquared = 100.0;
        double backgroundAndReadVariance = 20.0 + 2.0 * 2.0;
        double expectedPeakSignal = 0.5 * (
                targetSquared + Math.sqrt(
                        targetSquared * targetSquared
                                + 4.0 * targetSquared
                                * backgroundAndReadVariance
                )
        );
        double realizedPeakSignal = result.getParticlePhotonBudget(0)
                * peakWeight(appearance);
        assertEquals(expectedPeakSignal, realizedPeakSignal, 1.0e-9);
        assertEquals(10.0, realizedPeakSignal / Math.sqrt(
                realizedPeakSignal + backgroundAndReadVariance
        ), 1.0e-12);
    }

    @Test
    public void fixedInputsProduceIdenticalCalibration() {
        SnrNoiseCalibrator calibrator = new SnrNoiseCalibrator();
        PhotonCalibrationResult first = calibrator.calibrate(
                NoiseConfig.defaultConfig(),
                MicroscopeConfig.defaultConfig(),
                samples()
        );
        PhotonCalibrationResult second = calibrator.calibrate(
                NoiseConfig.defaultConfig(),
                MicroscopeConfig.defaultConfig(),
                samples()
        );
        assertEquals(first.getPhotonScale(), second.getPhotonScale(), 0.0);
        assertEquals(first.getParticlePhotonBudgets(),
                second.getParticlePhotonBudgets());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsDetectorLimitedTarget() {
        MicroscopeConfig microscope = new MicroscopeConfig(
                0.1,
                1.0 / 30.0,
                20.0,
                2.0,
                1.0,
                100.0,
                8
        );
        new SnrNoiseCalibrator().calibrate(
                noise(1000.0),
                microscope,
                samples()
        );
    }

    private NoiseConfig noise(double target) {
        return new NoiseConfig(target, SnrDefinition.PEAK_PIXEL_SIGNAL);
    }

    private ParticleAppearanceSample sample(double weight, double sigma) {
        return new ParticleAppearanceSample(
                new ParticlePsfParameters(
                        PsfSizeClass.MEDIUM,
                        PsfShapeType.CIRCULAR_GAUSSIAN,
                        5.0 * sigma,
                        sigma,
                        sigma,
                        0.0
                ),
                weight
        );
    }

    private double peakWeight(ParticleAppearanceSample appearance) {
        ParticlePsfParameters psf = appearance.getPsfParameters();
        int halfWidth = (int) Math.ceil(psf.getRadiusPixels());
        int side = 2 * halfWidth + 1;
        double[] pixels = new double[side * side];
        new CircularGaussianPsfRenderer().render(
                pixels,
                side,
                side,
                halfWidth + 0.5,
                halfWidth + 0.5,
                new ParticleImagingParameters(psf, 1.0)
        );
        double peak = 0.0;
        for (double pixel : pixels) {
            peak = Math.max(peak, pixel);
        }
        return peak;
    }

    private List<ParticleAppearanceSample> samples() {
        List<ParticleAppearanceSample> result =
                new ArrayList<ParticleAppearanceSample>();
        result.add(sample(1000.0, 1.2));
        result.add(sample(Math.sqrt(2.0) * 1000.0, 1.4));
        result.add(sample(2000.0, 1.6));
        return result;
    }
}
