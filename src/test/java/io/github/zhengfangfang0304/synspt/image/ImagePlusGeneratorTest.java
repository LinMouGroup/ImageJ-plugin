package io.github.zhengfangfang0304.synspt.image;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.config.NoiseConfig;
import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.engine.DefaultSimulationEngine;
import io.github.zhengfangfang0304.synspt.engine.RenderedSimulationResult;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancellationToken;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancelledException;
import io.github.zhengfangfang0304.synspt.engine.SimulationProgressListener;
import io.github.zhengfangfang0304.synspt.engine.SimulationResult;
import io.github.zhengfangfang0304.synspt.noise.CameraFrame;
import io.github.zhengfangfang0304.synspt.noise.NoiseModel;
import io.github.zhengfangfang0304.synspt.noise.SnrDefinition;
import io.github.zhengfangfang0304.synspt.optical.ExpectedPhotonFrame;
import io.github.zhengfangfang0304.synspt.optical.MicroscopyFrameGenerator;
import io.github.zhengfangfang0304.synspt.optical.OpticalRenderer;
import io.github.zhengfangfang0304.synspt.scene.Scene;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ImagePlusGeneratorTest {

    @Test
    public void buildsCalibratedSixteenBitImageJTimeStack() {
        SimulationConfig config = config(16, 4);
        SimulationResult simulationResult = simulate(config);
        RenderedSimulationResult rendered = new ImagePlusGenerator().generate(
                simulationResult,
                SimulationProgressListener.NONE
        );
        ImagePlus imagePlus = rendered.getImagePlus();
        Calibration calibration = imagePlus.getCalibration();

        assertSame(config, rendered.getPlan().getRequestConfig());
        assertEquals("synSPT Synthetic Data", ImagePlusGenerator.DEFAULT_IMAGE_TITLE);
        assertEquals("synSPT Synthetic Data", imagePlus.getTitle());
        assertEquals(16, imagePlus.getWidth());
        assertEquals(12, imagePlus.getHeight());
        assertEquals(4, imagePlus.getStackSize());
        assertEquals(1, imagePlus.getNChannels());
        assertEquals(1, imagePlus.getNSlices());
        assertEquals(4, imagePlus.getNFrames());
        assertEquals(16, imagePlus.getBitDepth());
        assertTrue(imagePlus.getStack().getPixels(1) instanceof short[]);
        assertEquals("Frame 1", imagePlus.getStack().getSliceLabel(1));
        assertEquals(config.getMicroscopeConfig().getPixelSizeUm(),
                calibration.pixelWidth, 0.0);
        assertEquals(config.getMicroscopeConfig().getPixelSizeUm(),
                calibration.pixelHeight, 0.0);
        assertEquals(config.getMicroscopeConfig().getFrameIntervalSeconds(),
                calibration.frameInterval, 0.0);
        assertEquals("µm", calibration.getUnit());
        assertEquals("s", calibration.getTimeUnit());
    }

    @Test
    public void usesBackgroundAndSignalStatisticsForDisplay() {
        ImagePlus imagePlus = new ImagePlusGenerator().generate(
                simulate(config(16, 4)),
                SimulationProgressListener.NONE
        ).getImagePlus();

        assertTrue(imagePlus.getDisplayRangeMin() < 120.0);
        assertTrue(imagePlus.getDisplayRangeMin() > 90.0);
        assertTrue(imagePlus.getDisplayRangeMax() > 120.0);
        assertTrue(imagePlus.getDisplayRangeMax() < 65535.0);
    }

    @Test
    public void signalPercentileRejectsRareBrightOutlierWithoutChangingPixels() {
        final short[] expectedPixels = new short[1000];
        for (int index = 0; index < 800; index++) {
            expectedPixels[index] = (short) 100;
        }
        for (int index = 800; index < 999; index++) {
            expectedPixels[index] = (short) 500;
        }
        expectedPixels[999] = (short) 5000;

        OpticalRenderer flatOpticalRenderer = new OpticalRenderer() {
            @Override
            public ExpectedPhotonFrame renderFrame(
                    Scene scene,
                    int frameIndex,
                    MicroscopeConfig microscopeConfig
            ) {
                return new ExpectedPhotonFrame(
                        scene.getWidthPixels(),
                        scene.getHeightPixels(),
                        new double[scene.getWidthPixels() * scene.getHeightPixels()]
                );
            }
        };
        NoiseModel deterministicNoiseModel = new NoiseModel() {
            @Override
            public CameraFrame apply(
                    ExpectedPhotonFrame expectedPhotonFrame,
                    MicroscopeConfig microscopeConfig,
                    long randomSeed
            ) {
                return new CameraFrame(
                        expectedPhotonFrame.getWidth(),
                        expectedPhotonFrame.getHeight(),
                        microscopeConfig.getBitDepth(),
                        expectedPixels
                );
            }
        };
        ImagePlusGenerator generator = new ImagePlusGenerator(
                new MicroscopyFrameGenerator(
                        flatOpticalRenderer,
                        deterministicNoiseModel
                )
        );
        SimulationConfig outlierConfig = SimulationConfig.builder()
                .imageWidth(100)
                .imageHeight(10)
                .frames(1)
                .particleNumber(2)
                .microscopeConfig(new MicroscopeConfig(
                        0.1,
                        1.0 / 30.0,
                        20.0,
                        2.0,
                        1.0,
                        100.0,
                        16
                ))
                .randomSeed(50505L)
                .build();

        ImagePlus imagePlus = generator.generate(
                simulate(outlierConfig),
                SimulationProgressListener.NONE
        ).getImagePlus();

        assertEquals(100.0, imagePlus.getDisplayRangeMin(), 0.0);
        assertEquals(500.0, imagePlus.getDisplayRangeMax(), 0.0);
        assertArrayEquals(expectedPixels,
                (short[]) imagePlus.getStack().getPixels(1));
        assertEquals(5000,
                ((short[]) imagePlus.getStack().getPixels(1))[999] & 0xffff);
    }

    @Test
    public void backgroundStatisticsControlLowRangeAndSignalControlsHighRange() {
        short[] expectedPixels = new short[10000];
        Random random = new Random(90909L);
        for (int index = 0; index < 9500; index++) {
            expectedPixels[index] = (short) Math.max(
                    0,
                    Math.round(100.0 + 10.0 * random.nextGaussian())
            );
        }
        for (int index = 9500; index < expectedPixels.length - 1; index++) {
            expectedPixels[index] = (short) (300 + index % 11);
        }
        expectedPixels[expectedPixels.length - 1] = (short) 5000;

        ImagePlus imagePlus = generateFromPixels(
                expectedPixels,
                100,
                100,
                16
        );

        assertEquals(75.0, imagePlus.getDisplayRangeMin(), 3.0);
        assertTrue(imagePlus.getDisplayRangeMax() >= 300.0);
        assertTrue(imagePlus.getDisplayRangeMax() <= 310.0);
        assertArrayEquals(expectedPixels,
                (short[]) imagePlus.getStack().getPixels(1));
    }

    @Test
    public void displayConversionHasVisibleGrayscaleContrast() {
        ImagePlus imagePlus = new ImagePlusGenerator().generate(
                simulate(config(16, 4)),
                SimulationProgressListener.NONE
        ).getImagePlus();

        ImageProcessor displayed = imagePlus.getProcessor().convertToByte(true);
        byte[] displayedPixels = (byte[]) displayed.getPixels();
        int minimum = 255;
        int maximum = 0;
        for (byte pixel : displayedPixels) {
            int value = pixel & 0xff;
            minimum = Math.min(minimum, value);
            maximum = Math.max(maximum, value);
        }

        assertTrue(maximum - minimum > 1);
    }

    @Test
    public void convertsEightBitCameraFramesToByteSlices() {
        SimulationConfig config = config(8, 2);
        SimulationResult simulationResult = simulate(config);
        ImagePlus imagePlus = new ImagePlusGenerator().generate(
                simulationResult,
                SimulationProgressListener.NONE
        ).getImagePlus();

        assertEquals(8, imagePlus.getBitDepth());
        assertTrue(imagePlus.getStack().getPixels(1) instanceof byte[]);
        assertTrue(imagePlus.getDisplayRangeMin() >= 0.0);
        assertTrue(imagePlus.getDisplayRangeMax() <= 255.0);
        assertTrue(imagePlus.getDisplayRangeMax()
                > imagePlus.getDisplayRangeMin());
    }

    @Test
    public void imagePixelsExactlyMatchTheGeneratedCameraFrame() {
        SimulationConfig config = config(16, 2);
        SimulationResult simulationResult = simulate(config);
        ImagePlus imagePlus = new ImagePlusGenerator().generate(
                simulationResult,
                SimulationProgressListener.NONE
        ).getImagePlus();

        MicroscopyFrameGenerator frameGenerator = new MicroscopyFrameGenerator();
        for (int frameIndex = 0; frameIndex < config.getFrames(); frameIndex++) {
            CameraFrame expectedFrame = frameGenerator.generateCameraFrame(
                    simulationResult,
                    frameIndex
            );
            assertArrayEquals(
                    expectedFrame.copyPixels(),
                    (short[]) imagePlus.getStack().getPixels(frameIndex + 1)
            );
        }
    }

    @Test
    public void expandsDegenerateDisplayRangeWithoutChangingFlatPixels() {
        OpticalRenderer flatOpticalRenderer = new OpticalRenderer() {
            @Override
            public ExpectedPhotonFrame renderFrame(
                    Scene scene,
                    int frameIndex,
                    MicroscopeConfig microscopeConfig
            ) {
                return new ExpectedPhotonFrame(
                        scene.getWidthPixels(),
                        scene.getHeightPixels(),
                        new double[scene.getWidthPixels() * scene.getHeightPixels()]
                );
            }
        };
        NoiseModel flatNoiseModel = new NoiseModel() {
            @Override
            public CameraFrame apply(
                    ExpectedPhotonFrame expectedPhotonFrame,
                    MicroscopeConfig microscopeConfig,
                    long randomSeed
            ) {
                return new CameraFrame(
                        expectedPhotonFrame.getWidth(),
                        expectedPhotonFrame.getHeight(),
                        microscopeConfig.getBitDepth(),
                        new short[expectedPhotonFrame.getPixelCount()]
                );
            }
        };
        ImagePlusGenerator generator = new ImagePlusGenerator(
                new MicroscopyFrameGenerator(flatOpticalRenderer, flatNoiseModel)
        );

        ImagePlus imagePlus = generator.generate(
                simulate(config(16, 2)),
                SimulationProgressListener.NONE
        ).getImagePlus();

        assertEquals(0.0, imagePlus.getDisplayRangeMin(), 0.0);
        assertEquals(1.0, imagePlus.getDisplayRangeMax(), 0.0);
        for (int slice = 1; slice <= imagePlus.getStackSize(); slice++) {
            for (short pixel : (short[]) imagePlus.getStack().getPixels(slice)) {
                assertEquals(0, pixel & 0xffff);
            }
        }
    }

    @Test
    public void imageGenerationReportsMonotonicProgress() {
        final List<Integer> percentages = new ArrayList<Integer>();
        new ImagePlusGenerator().generate(
                simulate(config(16, 5)),
                new SimulationProgressListener() {
                    @Override
                    public void onProgress(int percent, String message) {
                        percentages.add(percent);
                        assertTrue(message != null && !message.isEmpty());
                    }
                }
        );

        assertEquals(0, percentages.get(0).intValue());
        assertEquals(100, percentages.get(percentages.size() - 1).intValue());
        for (int index = 1; index < percentages.size(); index++) {
            assertTrue(percentages.get(index) >= percentages.get(index - 1));
        }
    }

    @Test(expected = SimulationCancelledException.class)
    public void imageGenerationCanBeCancelledBeforeTheFirstFrame() {
        final boolean[] cancelled = new boolean[] {false};
        new ImagePlusGenerator().generate(
                simulate(config(16, 5)),
                new SimulationProgressListener() {
                    @Override
                    public void onProgress(int percent, String message) {
                        cancelled[0] = true;
                    }
                },
                new SimulationCancellationToken() {
                    @Override
                    public boolean isCancellationRequested() {
                        return cancelled[0];
                    }
                }
        );
    }

    private SimulationResult simulate(SimulationConfig config) {
        return new DefaultSimulationEngine().generate(
                config,
                SimulationProgressListener.NONE
        );
    }

    private SimulationConfig config(int bitDepth, int frames) {
        MicroscopeConfig microscopeConfig = new MicroscopeConfig(
                0.1,
                1.0 / 30.0,
                20.0,
                2.0,
                1.0,
                100.0,
                bitDepth
        );
        SimulationConfig.Builder builder = SimulationConfig.builder()
                .imageWidth(16)
                .imageHeight(12)
                .frames(frames)
                .particleNumber(2)
                .microscopeConfig(microscopeConfig)
                .randomSeed(50505L);
        if (bitDepth == 8) {
            builder.noiseConfig(new NoiseConfig(
                    5.0,
                    SnrDefinition.PEAK_PIXEL_SIGNAL
            ));
        }
        return builder.build();
    }

    private ImagePlus generateFromPixels(
            final short[] expectedPixels,
            int width,
            int height,
            int bitDepth
    ) {
        OpticalRenderer flatOpticalRenderer = new OpticalRenderer() {
            @Override
            public ExpectedPhotonFrame renderFrame(
                    Scene scene,
                    int frameIndex,
                    MicroscopeConfig microscopeConfig
            ) {
                return new ExpectedPhotonFrame(
                        scene.getWidthPixels(),
                        scene.getHeightPixels(),
                        new double[scene.getWidthPixels() * scene.getHeightPixels()]
                );
            }
        };
        NoiseModel deterministicNoiseModel = new NoiseModel() {
            @Override
            public CameraFrame apply(
                    ExpectedPhotonFrame expectedPhotonFrame,
                    MicroscopeConfig microscopeConfig,
                    long randomSeed
            ) {
                return new CameraFrame(
                        expectedPhotonFrame.getWidth(),
                        expectedPhotonFrame.getHeight(),
                        microscopeConfig.getBitDepth(),
                        expectedPixels
                );
            }
        };
        ImagePlusGenerator generator = new ImagePlusGenerator(
                new MicroscopyFrameGenerator(
                        flatOpticalRenderer,
                        deterministicNoiseModel
                )
        );
        SimulationConfig displayConfig = SimulationConfig.builder()
                .imageWidth(width)
                .imageHeight(height)
                .frames(1)
                .particleNumber(2)
                .microscopeConfig(new MicroscopeConfig(
                        0.1,
                        1.0 / 30.0,
                        20.0,
                        2.0,
                        1.0,
                        100.0,
                        bitDepth
                ))
                .randomSeed(50505L)
                .build();
        return generator.generate(
                simulate(displayConfig),
                SimulationProgressListener.NONE
        ).getImagePlus();
    }
}
