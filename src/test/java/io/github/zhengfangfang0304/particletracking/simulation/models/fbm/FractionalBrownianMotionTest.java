package io.github.zhengfangfang0304.particletracking.simulation.models.fbm;

import io.github.zhengfangfang0304.particletracking.simulation.config.MotionSelectionConfig;
import io.github.zhengfangfang0304.particletracking.simulation.config.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticDataset;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticParticle;
import io.github.zhengfangfang0304.particletracking.simulation.generators.SyntheticDatasetGenerator;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.EnumSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FractionalBrownianMotionTest {

    private static final int STATISTICAL_SAMPLE_SIZE = 16_384;

    @Test
    void outputLengthIncludesInitialZero() {
        FractionalBrownianMotion motion =
                createMotion(0.7, 1L);

        double[] trajectory = motion.generate(128);

        assertEquals(129, trajectory.length);
        assertEquals(0.0, trajectory[0]);
    }

    @Test
    void hurstOneHalfBehavesLikeBrownianMotion() {
        double[] increments =
                increments(
                        createMotion(0.5, 2L)
                                .generate(STATISTICAL_SAMPLE_SIZE)
                );

        double variance = sampleVariance(increments);
        double lagOneCorrelation =
                lagOneCorrelation(increments);

        assertTrue(
                variance > 0.90 && variance < 1.10,
                "H=0.5 increments should have Brownian variance."
        );
        assertTrue(
                Math.abs(lagOneCorrelation) < 0.06,
                "H=0.5 increments should be approximately independent."
        );
    }

    @Test
    void hurstAboveOneHalfHasPositivelyCorrelatedIncrements() {
        double[] increments =
                increments(
                        createMotion(0.75, 3L)
                                .generate(STATISTICAL_SAMPLE_SIZE)
                );

        assertTrue(
                lagOneCorrelation(increments) > 0.20,
                "Persistent FBM increments should be positively correlated."
        );
    }

    @Test
    void hurstBelowOneHalfHasNegativelyCorrelatedIncrements() {
        double[] increments =
                increments(
                        createMotion(0.25, 4L)
                                .generate(STATISTICAL_SAMPLE_SIZE)
                );

        assertTrue(
                lagOneCorrelation(increments) < -0.15,
                "Anti-persistent FBM increments should be negatively correlated."
        );
    }

    @Test
    void daviesHarteFailureFallsBackToHosking() {
        DaviesHarteGenerator failingDaviesHarte =
                new DaviesHarteGenerator() {
                    @Override
                    public double[] generate(
                            int length,
                            double hurst,
                            Random random
                    ) {
                        throw new DaviesHarteException(
                                "Forced invalid embedding."
                        );
                    }
                };
        FractionalGaussianNoise noise =
                new FractionalGaussianNoise(
                        0.7,
                        new Random(5L),
                        failingDaviesHarte,
                        new HoskingGenerator()
                );
        FractionalBrownianMotion motion =
                new FractionalBrownianMotion(noise);

        double[] trajectory = motion.generate(64);

        assertEquals(65, trajectory.length);
        assertTrue(motion.usedHoskingFallback());
    }

    @Test
    void frameworkCombinesFbmAxesIntoParticleTrajectoryMaps() {
        SimulationConfig config = new SimulationConfig();
        config.width = 64;
        config.height = 48;
        config.frames = 20;
        config.particleNumber = 2;
        config.setMotionSelectionConfig(
                new MotionSelectionConfig(
                        EnumSet.of(MotionType.SUPERDIFFUSION)
                )
        );

        SyntheticDataset dataset =
                new SyntheticDatasetGenerator().generate(
                        config,
                        707L
                );

        assertEquals(2, dataset.getParticles().size());
        for (SyntheticParticle particle : dataset.getParticles()) {
            assertEquals(20, particle.getTrajectory().size());

            for (Point2D.Double point
                    : particle.getTrajectory().values()) {
                assertTrue(point.x >= 0.0 && point.x < config.width);
                assertTrue(point.y >= 0.0 && point.y < config.height);
            }
        }
    }

    @Test
    void fbmValidatesHurstAtConstruction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FractionalBrownianMotion(0.0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new FractionalBrownianMotion(1.0)
        );
    }

    private FractionalBrownianMotion createMotion(
            double hurst,
            long seed
    ) {
        return new FractionalBrownianMotion(
                hurst,
                new Random(seed)
        );
    }

    private double[] increments(double[] trajectory) {
        double[] increments =
                new double[trajectory.length - 1];

        for (int i = 0; i < increments.length; i++) {
            increments[i] =
                    trajectory[i + 1] - trajectory[i];
        }

        return increments;
    }

    private double sampleVariance(double[] values) {
        double mean = mean(values);
        double sum = 0.0;

        for (double value : values) {
            double centered = value - mean;
            sum += centered * centered;
        }

        return sum / (values.length - 1);
    }

    private double lagOneCorrelation(double[] values) {
        double mean = mean(values);
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

    private double mean(double[] values) {
        double sum = 0.0;

        for (double value : values) {
            sum += value;
        }

        return sum / values.length;
    }
}
