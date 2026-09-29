package io.github.zhengfangfang0304.synspt.image;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.engine.RenderedSimulationResult;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancellationToken;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancelledException;
import io.github.zhengfangfang0304.synspt.engine.SimulationProgressListener;
import io.github.zhengfangfang0304.synspt.engine.SimulationResult;
import io.github.zhengfangfang0304.synspt.noise.CameraFrame;
import io.github.zhengfangfang0304.synspt.optical.MicroscopyFrameGenerator;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;

/** Streams optical camera frames into a calibrated ImageJ time stack. */
public final class ImagePlusGenerator {

    public static final String DEFAULT_IMAGE_TITLE = "synSPT Synthetic Data";

    private static final int FRAME_RENDERING_PROGRESS_RANGE = 95;
    private static final double DEFAULT_DISPLAY_LOW_BACKGROUND_SIGMA = 2.5;
    private static final double DEFAULT_SIGNAL_THRESHOLD_SIGMA = 3.0;
    private static final double DEFAULT_SIGNAL_HIGH_PERCENTILE = 99.0;
    private static final double BACKGROUND_MEDIAN_PERCENTILE = 50.0;
    private static final double BACKGROUND_LOW_SIGMA_PERCENTILE =
            15.8655253931457;
    private static final double BACKGROUND_CLIP_SIGMA = 3.0;
    private static final double NO_SIGNAL_DISPLAY_HIGH_SIGMA = 6.0;

    private final MicroscopyFrameGenerator frameGenerator;

    public ImagePlusGenerator() {
        this(new MicroscopyFrameGenerator());
    }

    public ImagePlusGenerator(MicroscopyFrameGenerator frameGenerator) {
        if (frameGenerator == null) {
            throw new IllegalArgumentException("Frame generator cannot be null.");
        }
        this.frameGenerator = frameGenerator;
    }

    public RenderedSimulationResult generate(
            SimulationResult simulationResult,
            SimulationProgressListener progressListener
    ) {
        return generate(
                simulationResult,
                progressListener,
                SimulationCancellationToken.NONE
        );
    }

    public RenderedSimulationResult generate(
            SimulationResult simulationResult,
            SimulationProgressListener progressListener,
            SimulationCancellationToken cancellationToken
    ) {
        if (simulationResult == null
                || progressListener == null
                || cancellationToken == null) {
            throw new IllegalArgumentException(
                    "Simulation result, progress listener, and cancellation token cannot be null."
            );
        }
        checkCancellation(cancellationToken);
        progressListener.onProgress(0, "Preparing ImageJ stack");

        int width = simulationResult.getPlan().getRequestConfig().getImageWidth();
        int height = simulationResult.getPlan().getRequestConfig().getImageHeight();
        int frameCount = simulationResult.getPlan().getRequestConfig().getFrames();
        int bitDepth = simulationResult.getPlan().getRequestConfig()
                .getMicroscopeConfig().getBitDepth();
        ImageStack imageStack = new ImageStack(width, height);
        DisplayRangeAccumulator displayRange = new DisplayRangeAccumulator();

        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            checkCancellation(cancellationToken);
            CameraFrame cameraFrame = frameGenerator.generateCameraFrame(
                    simulationResult,
                    frameIndex
            );
            addSlice(imageStack, cameraFrame, frameIndex, displayRange);
            int progress = FRAME_RENDERING_PROGRESS_RANGE
                    * (frameIndex + 1) / frameCount;
            progressListener.onProgress(
                    progress,
                    "Rendered image frame " + (frameIndex + 1) + " of " + frameCount
            );
        }

        checkCancellation(cancellationToken);
        ImagePlus imagePlus = new ImagePlus(DEFAULT_IMAGE_TITLE, imageStack);
        imagePlus.setDimensions(1, 1, frameCount);
        imagePlus.setOpenAsHyperStack(frameCount > 1);
        applyCalibration(
                imagePlus,
                simulationResult.getPlan().getRequestConfig().getMicroscopeConfig()
        );
        applyDisplayRange(imagePlus, displayRange, bitDepth);
        progressListener.onProgress(100, "ImagePlus stack complete");
        return new RenderedSimulationResult(simulationResult, imagePlus);
    }

    private void addSlice(
            ImageStack imageStack,
            CameraFrame cameraFrame,
            int frameIndex,
            DisplayRangeAccumulator displayRange
    ) {
        String label = "Frame " + (frameIndex + 1);
        short[] pixels = cameraFrame.copyPixels();
        displayRange.addPixels(pixels);
        if (cameraFrame.getBitDepth() == 8) {
            byte[] bytePixels = new byte[pixels.length];
            for (int index = 0; index < pixels.length; index++) {
                bytePixels[index] = (byte) (pixels[index] & 0xff);
            }
            imageStack.addSlice(label, bytePixels);
        } else {
            imageStack.addSlice(label, pixels);
        }
    }

    private void applyDisplayRange(
            ImagePlus imagePlus,
            DisplayRangeAccumulator displayRange,
            int bitDepth
    ) {
        DisplayRange range = displayRange.calculateDisplayRange(bitDepth);

        /*
         * Display optimization only, does not modify raw image data.
         * ImagePlus stores this range in its display LUT; stack pixels remain
         * the exact digitized CameraFrame samples used for TIFF export.
         */
        imagePlus.setDisplayRange(range.minimumAdu, range.maximumAdu);
    }

    private void applyCalibration(
            ImagePlus imagePlus,
            MicroscopeConfig microscopeConfig
    ) {
        Calibration calibration = new Calibration();
        calibration.pixelWidth = microscopeConfig.getPixelSizeUm();
        calibration.pixelHeight = microscopeConfig.getPixelSizeUm();
        calibration.frameInterval = microscopeConfig.getFrameIntervalSeconds();
        calibration.setUnit("µm");
        calibration.setTimeUnit("s");
        imagePlus.setCalibration(calibration);
    }

    private void checkCancellation(
            SimulationCancellationToken cancellationToken
    ) {
        if (cancellationToken.isCancellationRequested()) {
            throw new SimulationCancelledException();
        }
    }

    private static final class DisplayRangeAccumulator {

        private static final int UNSIGNED_SHORT_VALUE_COUNT = 1 << 16;

        private final long[] histogram = new long[UNSIGNED_SHORT_VALUE_COUNT];
        private long pixelCount;

        private void addPixels(short[] pixels) {
            for (short pixel : pixels) {
                int unsignedAdu = pixel & 0xffff;
                histogram[unsignedAdu]++;
                pixelCount++;
            }
        }

        private DisplayRange calculateDisplayRange(int bitDepth) {
            BackgroundStatistics background = calculateBackgroundStatistics();
            int sensorMaximumAdu = (1 << bitDepth) - 1;
            double minimumAdu = clamp(
                    background.mean - DEFAULT_DISPLAY_LOW_BACKGROUND_SIGMA
                            * background.standardDeviation,
                    0.0,
                    sensorMaximumAdu
            );

            int signalThreshold = (int) Math.floor(
                    background.mean + DEFAULT_SIGNAL_THRESHOLD_SIGMA
                            * background.standardDeviation
            );
            int signalPercentile = calculateUpperTailPercentile(
                    signalThreshold,
                    DEFAULT_SIGNAL_HIGH_PERCENTILE
            );
            double maximumAdu;
            if (signalPercentile >= 0) {
                maximumAdu = signalPercentile;
            } else if (background.standardDeviation > 0.0) {
                maximumAdu = background.mean
                        + NO_SIGNAL_DISPLAY_HIGH_SIGMA
                        * background.standardDeviation;
            } else {
                maximumAdu = background.mean + 1.0;
            }
            maximumAdu = clamp(maximumAdu, 0.0, sensorMaximumAdu);
            if (maximumAdu <= minimumAdu) {
                if (minimumAdu < sensorMaximumAdu) {
                    maximumAdu = minimumAdu + 1.0;
                } else {
                    minimumAdu = Math.max(0.0, minimumAdu - 1.0);
                }
            }
            return new DisplayRange(minimumAdu, maximumAdu);
        }

        private BackgroundStatistics calculateBackgroundStatistics() {
            int median = calculatePercentile(BACKGROUND_MEDIAN_PERCENTILE);
            int lowerSigmaPoint = calculatePercentile(
                    BACKGROUND_LOW_SIGMA_PERCENTILE
            );
            double sigmaSeed = median - lowerSigmaPoint;
            if (sigmaSeed <= 0.0) {
                return new BackgroundStatistics(median, 0.0);
            }

            int lowerBound = Math.max(
                    0,
                    (int) Math.floor(median
                            - BACKGROUND_CLIP_SIGMA * sigmaSeed)
            );
            int upperBound = Math.min(
                    histogram.length - 1,
                    (int) Math.ceil(median
                            + BACKGROUND_CLIP_SIGMA * sigmaSeed)
            );
            long includedPixels = 0L;
            double sum = 0.0;
            double squaredSum = 0.0;
            for (int value = lowerBound; value <= upperBound; value++) {
                long count = histogram[value];
                includedPixels += count;
                sum += value * (double) count;
                squaredSum += value * (double) value * count;
            }
            if (includedPixels == 0L) {
                return new BackgroundStatistics(median, sigmaSeed);
            }
            double mean = sum / includedPixels;
            double variance = Math.max(
                    0.0,
                    squaredSum / includedPixels - mean * mean
            );
            return new BackgroundStatistics(mean, Math.sqrt(variance));
        }

        private int calculateUpperTailPercentile(
                int exclusiveLowerBound,
                double percentile
        ) {
            int firstValue = Math.max(0, exclusiveLowerBound + 1);
            if (firstValue >= histogram.length) {
                return -1;
            }
            long upperTailCount = 0L;
            for (int value = firstValue; value < histogram.length; value++) {
                upperTailCount += histogram[value];
            }
            if (upperTailCount == 0L) {
                return -1;
            }
            long targetCount = Math.max(
                    1L,
                    (long) Math.ceil(percentile * upperTailCount / 100.0)
            );
            long cumulativeCount = 0L;
            for (int value = firstValue; value < histogram.length; value++) {
                cumulativeCount += histogram[value];
                if (cumulativeCount >= targetCount) {
                    return value;
                }
            }
            return histogram.length - 1;
        }

        private int calculatePercentile(double percentile) {
            if (Double.isNaN(percentile)
                    || Double.isInfinite(percentile)
                    || percentile < 0.0
                    || percentile > 100.0) {
                throw new IllegalArgumentException(
                        "Display percentile must be between zero and one hundred."
                );
            }
            if (pixelCount == 0L) {
                throw new IllegalStateException(
                        "Cannot calculate a display percentile without pixels."
                );
            }

            long targetCount = (long) Math.ceil(
                    percentile * pixelCount / 100.0
            );
            targetCount = Math.max(1L, targetCount);
            long cumulativeCount = 0L;
            for (int value = 0; value < histogram.length; value++) {
                cumulativeCount += histogram[value];
                if (cumulativeCount >= targetCount) {
                    return value;
                }
            }
            return histogram.length - 1;
        }

        private double clamp(double value, double minimum, double maximum) {
            return Math.max(minimum, Math.min(maximum, value));
        }
    }

    private static final class BackgroundStatistics {

        private final double mean;
        private final double standardDeviation;

        private BackgroundStatistics(double mean, double standardDeviation) {
            this.mean = mean;
            this.standardDeviation = standardDeviation;
        }
    }

    private static final class DisplayRange {

        private final double minimumAdu;
        private final double maximumAdu;

        private DisplayRange(double minimumAdu, double maximumAdu) {
            this.minimumAdu = minimumAdu;
            this.maximumAdu = maximumAdu;
        }
    }
}
