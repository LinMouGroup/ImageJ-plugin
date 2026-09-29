package io.github.zhengfangfang0304.particletracking.simulation.physics;

import ij.measure.Calibration;
import io.github.zhengfangfang0304.particletracking.simulation.config.ImagingConfig;
import io.github.zhengfangfang0304.particletracking.simulation.config.MotionSelectionConfig;
import io.github.zhengfangfang0304.particletracking.simulation.config.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticDataset;
import io.github.zhengfangfang0304.particletracking.simulation.generators.FBMTrajectoryGenerator;
import io.github.zhengfangfang0304.particletracking.simulation.generators.SyntheticDatasetGenerator;
import io.github.zhengfangfang0304.particletracking.simulation.generators.TrajectoryGenerator;
import io.github.zhengfangfang0304.particletracking.simulation.models.fbm.FractionalBrownianMotion;
import io.github.zhengfangfang0304.particletracking.simulation.motion.DriftModel;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfile;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfileResolver;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;
import io.github.zhengfangfang0304.particletracking.simulation.motion.ParticleMotionAssignment;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicalScalingTest {

    private static final double TOLERANCE = 1.0e-12;

    private final PhysicalScaler scaler = new PhysicalScaler();

    private final MotionProfileResolver resolver =
            new MotionProfileResolver();

    @Test
    void normalProfileSeparatesDimensionlessPhysicalAndPixelPaths() {
        ImagingConfig imagingConfig =
                new ImagingConfig(0.1, 0.05);
        MotionProfile normalProfile = new MotionProfile(
                "normal-reference",
                "1.0",
                MotionType.NORMAL_DIFFUSION,
                0.5,
                0.4,
                DriftModel.none()
        );
        double[] dimensionless = {0.0, 1.0, 2.0, 3.0};
        double[] original = dimensionless.clone();

        double[] physical = scaler.scaleFractionalBrownian(
                dimensionless,
                normalProfile,
                imagingConfig
        );
        double[] pixels = scaler.toPixels(
                physical,
                imagingConfig
        );

        assertArrayEquals(
                new double[]{0.0, 0.2, 0.4, 0.6},
                physical,
                TOLERANCE
        );
        assertArrayEquals(
                new double[]{0.0, 2.0, 4.0, 6.0},
                pixels,
                TOLERANCE
        );
        assertArrayEquals(original, dimensionless, TOLERANCE);
        assertNotSame(dimensionless, physical);
        assertNotSame(physical, pixels);
    }

    @Test
    void trajectoryGeneratorAppliesProfileScalingThenPixelConversion() {
        SimulationConfig config = new SimulationConfig();
        config.width = 100;
        config.height = 100;
        config.frames = 3;
        config.setImagingConfig(new ImagingConfig(0.1, 0.05));
        MotionProfile profile = resolver.resolve(
                MotionType.NORMAL_DIFFUSION
        );
        ParticleMotionAssignment assignment =
                new ParticleMotionAssignment(profile, 808L);
        Point2D.Double initial = new Point2D.Double(50.0, 50.0);

        List<Point2D.Double> actual =
                new TrajectoryGenerator(scaler)
                        .generateTrajectory(
                                initial,
                                config,
                                assignment
                        );
        Random referenceRandom = new Random(808L);
        FBMTrajectoryGenerator fbmGenerator =
                new FBMTrajectoryGenerator();
        double[] expectedX = expectedPixels(
                fbmGenerator.generate(
                        2,
                        profile,
                        referenceRandom
                ),
                profile,
                config.getImagingConfig()
        );
        double[] expectedY = expectedPixels(
                fbmGenerator.generate(
                        2,
                        profile,
                        referenceRandom
                ),
                profile,
                config.getImagingConfig()
        );

        assertEquals(3, actual.size());
        for (int sample = 0; sample < actual.size(); sample++) {
            assertEquals(
                    initial.x + expectedX[sample],
                    actual.get(sample).x,
                    TOLERANCE
            );
            assertEquals(
                    initial.y + expectedY[sample],
                    actual.get(sample).y,
                    TOLERANCE
            );
        }
    }

    @Test
    void profileScalingUsesHurstTimeExponent() {
        ImagingConfig imagingConfig =
                new ImagingConfig(0.1, 0.05);
        double[] dimensionless = {0.0, 1.0, -2.0};
        MotionProfile persistent = profile(0.75);
        MotionProfile antiPersistent = profile(0.25);

        double[] persistentPhysical =
                scaler.scaleFractionalBrownian(
                        dimensionless,
                        persistent,
                        imagingConfig
                );
        double[] antiPersistentPhysical =
                scaler.scaleFractionalBrownian(
                        dimensionless,
                        antiPersistent,
                        imagingConfig
                );

        assertEquals(
                Math.pow(0.05, 0.75),
                persistentPhysical[1],
                TOLERANCE
        );
        assertEquals(
                Math.pow(0.05, 0.25),
                antiPersistentPhysical[1],
                TOLERANCE
        );
        assertTrue(
                persistentPhysical[1]
                        < antiPersistentPhysical[1]
        );
    }

    @Test
    void scaledFbmRetainsHurstDependentIncrementCorrelation() {
        ImagingConfig imagingConfig =
                new ImagingConfig(0.1, 0.05);
        double[] persistent = scaledFbmIncrements(
                profile(0.75),
                imagingConfig,
                11L
        );
        double[] antiPersistent = scaledFbmIncrements(
                profile(0.25),
                imagingConfig,
                12L
        );

        assertTrue(lagOneCorrelation(persistent) > 0.20);
        assertTrue(lagOneCorrelation(antiPersistent) < -0.15);
    }

    @Test
    void datasetAndImageCarryMicroscopyCalibration() {
        SimulationConfig config = new SimulationConfig();
        config.width = 32;
        config.height = 24;
        config.frames = 4;
        config.particleNumber = 1;
        config.setImagingConfig(new ImagingConfig(0.1, 0.05));
        config.setMotionSelectionConfig(
                new MotionSelectionConfig(
                        EnumSet.of(MotionType.NORMAL_DIFFUSION)
                )
        );

        SyntheticDataset dataset =
                new SyntheticDatasetGenerator().generate(
                        config,
                        909L
                );
        Calibration calibration =
                dataset.getImage().getCalibration();

        assertEquals(0.1, dataset.getPixelSizeUmPerPixel());
        assertEquals(0.05, dataset.getFrameIntervalSeconds());
        assertEquals("um", dataset.getSpatialUnit());
        assertEquals("s", dataset.getTimeUnit());
        assertEquals(
                EnumSet.of(MotionType.NORMAL_DIFFUSION),
                dataset.getMotionTypes()
        );
        assertEquals(0.1, calibration.pixelWidth);
        assertEquals(0.1, calibration.pixelHeight);
        assertEquals("\u00b5m", calibration.getUnit());
        assertEquals(0.05, calibration.frameInterval);
        assertEquals("s", calibration.getTimeUnit());
    }

    private double[] expectedPixels(
            double[] dimensionless,
            MotionProfile profile,
            ImagingConfig imagingConfig
    ) {
        return scaler.toPixels(
                scaler.scaleFractionalBrownian(
                        dimensionless,
                        profile,
                        imagingConfig
                ),
                imagingConfig
        );
    }

    private MotionProfile profile(double hurst) {
        return new MotionProfile(
                "reference-" + hurst,
                "1.0",
                hurst < 0.5
                        ? MotionType.SUBDIFFUSION
                        : MotionType.SUPERDIFFUSION,
                hurst,
                0.5,
                DriftModel.none()
        );
    }

    private double[] scaledFbmIncrements(
            MotionProfile profile,
            ImagingConfig imagingConfig,
            long seed
    ) {
        double[] dimensionless =
                new FractionalBrownianMotion(
                        profile.getHurstExponent(),
                        new Random(seed)
                ).generate(16_384);
        double[] physical =
                scaler.scaleFractionalBrownian(
                        dimensionless,
                        profile,
                        imagingConfig
                );
        double[] increments =
                new double[physical.length - 1];

        for (int i = 0; i < increments.length; i++) {
            increments[i] = physical[i + 1] - physical[i];
        }

        return increments;
    }

    private double lagOneCorrelation(double[] values) {
        double mean = 0.0;
        for (double value : values) {
            mean += value;
        }
        mean /= values.length;

        double covariance = 0.0;
        double variance = 0.0;
        for (int i = 0; i < values.length; i++) {
            double centered = values[i] - mean;
            variance += centered * centered;

            if (i > 0) {
                covariance +=
                        centered * (values[i - 1] - mean);
            }
        }

        return covariance / variance;
    }
}
