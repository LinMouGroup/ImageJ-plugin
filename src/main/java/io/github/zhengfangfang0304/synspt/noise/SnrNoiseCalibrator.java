package io.github.zhengfangfang0304.synspt.noise;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.config.NoiseConfig;
import io.github.zhengfangfang0304.synspt.optical.CircularGaussianPsfRenderer;
import io.github.zhengfangfang0304.synspt.optical.EllipticalGaussianPsfRenderer;
import io.github.zhengfangfang0304.synspt.optical.ParticleAppearanceSample;
import io.github.zhengfangfang0304.synspt.optical.ParticleImagingParameters;
import io.github.zhengfangfang0304.synspt.optical.ParticlePsfParameters;
import io.github.zhengfangfang0304.synspt.optical.PsfKernelRenderer;
import io.github.zhengfangfang0304.synspt.optical.PsfShapeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calibrates absolute emitter photon budgets from a requested median SNR.
 *
 * <p>For peak-pixel particle signal {@code A}, background {@code B}
 * photons/pixel, and read-noise equivalent {@code sigmaR} photons/pixel,
 * the physical model is</p>
 *
 * <pre>R = A / sqrt(A + B + sigmaR^2), A = P * peakWeight</pre>
 *
 * <p>The closed-form inverse supplies a scale estimate; a deterministic
 * binary search then chooses the common photon scale whose median predicted
 * particle SNR equals the requested value. No synthetic Gaussian noise is
 * created by this calibration.</p>
 */
public final class SnrNoiseCalibrator {

    private static final int BINARY_SEARCH_STEPS = 128;

    public PhotonCalibrationResult calibrate(
            NoiseConfig noiseConfig,
            MicroscopeConfig microscopeConfig,
            List<ParticleAppearanceSample> samples
    ) {
        validate(noiseConfig, microscopeConfig, samples);
        List<Double> relative = normalizeBrightness(samples);
        List<PeakMeasurement> measurements = measureAll(samples);
        double maxScale = maxScale(
                microscopeConfig,
                relative,
                measurements
        );
        double maxSnr = medianSnr(
                maxScale,
                microscopeConfig,
                relative,
                measurements
        );
        double target = noiseConfig.getTargetSnr();
        if (target > maxSnr) {
            throw new IllegalArgumentException(
                    "Target SNR exceeds the detector-limited physical maximum of "
                            + maxSnr + "."
            );
        }
        double scale = solveScale(
                target,
                maxScale,
                microscopeConfig,
                relative,
                measurements
        );
        return createResult(
                noiseConfig,
                microscopeConfig,
                scale,
                maxSnr,
                relative,
                measurements
        );
    }

    private List<Double> normalizeBrightness(
            List<ParticleAppearanceSample> samples
    ) {
        List<Double> weights = new ArrayList<Double>(samples.size());
        for (ParticleAppearanceSample sample : samples) {
            weights.add(sample.getBrightnessWeight());
        }
        double medianWeight = median(weights);
        List<Double> relative = new ArrayList<Double>(weights.size());
        for (Double weight : weights) {
            relative.add(weight.doubleValue() / medianWeight);
        }
        return Collections.unmodifiableList(relative);
    }

    private List<PeakMeasurement> measureAll(
            List<ParticleAppearanceSample> samples
    ) {
        List<PeakMeasurement> measurements =
                new ArrayList<PeakMeasurement>(samples.size());
        for (ParticleAppearanceSample sample : samples) {
            measurements.add(measure(sample.getPsfParameters()));
        }
        return measurements;
    }

    private PeakMeasurement measure(ParticlePsfParameters psf) {
        int halfWidth = Math.max(
                1,
                (int) Math.ceil(psf.getRadiusPixels())
        );
        int side = 2 * halfWidth + 1;
        double[] unitPhotons = new double[side * side];
        PsfKernelRenderer renderer = renderer(psf.getShapeType());
        renderer.render(
                unitPhotons,
                side,
                side,
                halfWidth + 0.5,
                halfWidth + 0.5,
                new ParticleImagingParameters(psf, 1.0)
        );
        double peak = 0.0;
        for (double value : unitPhotons) {
            peak = Math.max(peak, value);
        }
        if (peak <= 0.0 || peak > 1.000000001) {
            throw new IllegalArgumentException(
                    "Invalid PSF peak-pixel measurement."
            );
        }
        return new PeakMeasurement(peak);
    }

    private PsfKernelRenderer renderer(PsfShapeType shapeType) {
        if (shapeType == PsfShapeType.CIRCULAR_GAUSSIAN) {
            return new CircularGaussianPsfRenderer();
        }
        if (shapeType == PsfShapeType.ELLIPTICAL_GAUSSIAN) {
            return new EllipticalGaussianPsfRenderer();
        }
        throw new IllegalArgumentException("Unsupported PSF shape.");
    }

    private double maxScale(
            MicroscopeConfig microscope,
            List<Double> relative,
            List<PeakMeasurement> measurements
    ) {
        double sensorMaximum = (1L << microscope.getBitDepth()) - 1L;
        double capacity = (sensorMaximum - microscope.getOffsetAdu())
                / microscope.getGainAduPerPhoton()
                - microscope.getBackgroundPhotons();
        if (capacity <= 0.0) {
            throw new IllegalArgumentException(
                    "Camera offset and background leave no detector capacity."
            );
        }
        double result = Double.POSITIVE_INFINITY;
        for (int index = 0; index < relative.size(); index++) {
            double particleLimit = capacity / measurements.get(index).peakWeight;
            result = Math.min(
                    result,
                    particleLimit / relative.get(index).doubleValue()
            );
        }
        return result;
    }

    private double particleSnr(
            double photonBudget,
            MicroscopeConfig microscope,
            PeakMeasurement measurement
    ) {
        double peakSignal = photonBudget * measurement.peakWeight;
        double readPhotons = microscope.getReadNoiseSigmaAdu()
                / microscope.getGainAduPerPhoton();
        double variance = peakSignal
                + microscope.getBackgroundPhotons()
                + readPhotons * readPhotons;
        return peakSignal / Math.sqrt(variance);
    }

    private double medianSnr(
            double scale,
            MicroscopeConfig microscope,
            List<Double> relative,
            List<PeakMeasurement> measurements
    ) {
        List<Double> values = new ArrayList<Double>(relative.size());
        for (int index = 0; index < relative.size(); index++) {
            values.add(particleSnr(
                    scale * relative.get(index).doubleValue(),
                    microscope,
                    measurements.get(index)
            ));
        }
        return median(values);
    }

    private PhotonCalibrationResult createResult(
            NoiseConfig noise,
            MicroscopeConfig microscope,
            double scale,
            double maxSnr,
            List<Double> relative,
            List<PeakMeasurement> measurements
    ) {
        List<Double> budgets = new ArrayList<Double>(relative.size());
        List<Double> snrs = new ArrayList<Double>(relative.size());
        for (int index = 0; index < relative.size(); index++) {
            double budget = scale * relative.get(index).doubleValue();
            budgets.add(budget);
            snrs.add(particleSnr(
                    budget,
                    microscope,
                    measurements.get(index)
            ));
        }
        double realized = median(snrs);
        return new PhotonCalibrationResult(
                noise.getTargetSnr(),
                noise.getSnrDefinition(),
                scale,
                median(budgets),
                realized,
                maxSnr,
                microscope.getBackgroundPhotons(),
                microscope.getReadNoiseSigmaAdu(),
                relative,
                budgets,
                snrs
        );
    }

    private double solveScale(
            double target,
            double upperBound,
            MicroscopeConfig microscope,
            List<Double> relative,
            List<PeakMeasurement> measurements
    ) {
        double lower = 0.0;
        double upper = upperBound;
        double estimate = Math.min(
                upperBound,
                requiredScaleEstimate(
                        target,
                        microscope,
                        relative,
                        measurements
                )
        );
        if (medianSnr(estimate, microscope, relative, measurements) < target) {
            lower = estimate;
        } else {
            upper = estimate;
        }
        for (int step = 0; step < BINARY_SEARCH_STEPS; step++) {
            double middle = lower + 0.5 * (upper - lower);
            double value = medianSnr(
                    middle,
                    microscope,
                    relative,
                    measurements
            );
            if (value < target) {
                lower = middle;
            } else {
                upper = middle;
            }
        }
        return lower + 0.5 * (upper - lower);
    }

    private double requiredScaleEstimate(
            double target,
            MicroscopeConfig microscope,
            List<Double> relative,
            List<PeakMeasurement> measurements
    ) {
        double readPhotons = microscope.getReadNoiseSigmaAdu()
                / microscope.getGainAduPerPhoton();
        List<Double> requiredScales =
                new ArrayList<Double>(relative.size());
        for (int index = 0; index < relative.size(); index++) {
            PeakMeasurement measurement = measurements.get(index);
            double peakSignal = requiredPeakSignalPhotons(
                    target,
                    microscope.getBackgroundPhotons(),
                    readPhotons
            );
            double totalPhotons = peakSignal / measurement.peakWeight;
            requiredScales.add(
                    totalPhotons / relative.get(index).doubleValue()
            );
        }
        return median(requiredScales);
    }

    private double requiredPeakSignalPhotons(
            double target,
            double backgroundPhotons,
            double readNoisePhotons
    ) {
        double targetSquared = target * target;
        double backgroundVariance = backgroundPhotons
                + readNoisePhotons * readNoisePhotons;
        return 0.5 * (targetSquared + Math.sqrt(
                targetSquared * targetSquared
                + 4.0 * targetSquared * backgroundVariance
        ));
    }

    private double median(List<Double> values) {
        List<Double> sorted = new ArrayList<Double>(values);
        Collections.sort(sorted);
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(middle).doubleValue();
        }
        return 0.5 * (sorted.get(middle - 1).doubleValue()
                + sorted.get(middle).doubleValue());
    }

    private void validate(
            NoiseConfig noiseConfig,
            MicroscopeConfig microscopeConfig,
            List<ParticleAppearanceSample> samples
    ) {
        if (noiseConfig == null || microscopeConfig == null || samples == null) {
            throw new IllegalArgumentException(
                    "Noise config, microscope config, and samples cannot be null."
            );
        }
        if (noiseConfig.getSnrDefinition()
                != SnrDefinition.PEAK_PIXEL_SIGNAL) {
            throw new IllegalArgumentException("Unsupported SNR definition.");
        }
        if (samples.isEmpty()) {
            throw new IllegalArgumentException("Appearance samples cannot be empty.");
        }
        for (ParticleAppearanceSample sample : samples) {
            if (sample == null) {
                throw new IllegalArgumentException(
                        "Appearance samples cannot contain null."
                );
            }
        }
    }

    private static final class PeakMeasurement {

        private final double peakWeight;

        private PeakMeasurement(double peak) {
            this.peakWeight = peak;
        }
    }
}
