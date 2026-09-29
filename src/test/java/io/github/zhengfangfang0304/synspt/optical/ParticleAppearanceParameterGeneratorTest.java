package io.github.zhengfangfang0304.synspt.optical;

import io.github.zhengfangfang0304.synspt.config.ParticleAppearanceDistributionConfig;
import io.github.zhengfangfang0304.synspt.config.PsfSizeMixtureConfig;
import io.github.zhengfangfang0304.synspt.util.SeedDerivation;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParticleAppearanceParameterGeneratorTest {

    @Test
    public void defaultDistributionUsesThreeOrderedSizeBands() {
        ParticleAppearanceDistributionConfig distribution =
                ParticleAppearanceDistributionConfig.defaultConfig();
        PsfSizeMixtureConfig mixture =
                distribution.getPsfSizeMixtureConfig();

        assertRange(mixture, PsfSizeClass.SMALL, 1.2, 1.5);
        assertRange(mixture, PsfSizeClass.MEDIUM, 1.6, 2.0);
        assertRange(mixture, PsfSizeClass.LARGE, 2.0, 2.5);
        assertEquals(9.0, mixture.getDirichletAlpha(PsfSizeClass.SMALL), 0.0);
        assertEquals(15.0, mixture.getDirichletAlpha(PsfSizeClass.MEDIUM), 0.0);
        assertEquals(6.0, mixture.getDirichletAlpha(PsfSizeClass.LARGE), 0.0);
        assertEquals(1000.0, distribution.getBrightnessWeightMin(), 0.0);
        assertEquals(5000.0, distribution.getBrightnessWeightMax(), 0.0);
        assertEquals(5.0, distribution.getBrightnessWeightMax()
                / distribution.getBrightnessWeightMin(), 0.0);
        assertEquals(0.25, distribution.getSizeBrightnessCorrelation(), 0.0);
    }

    @Test
    public void fixedSeedsProduceIdenticalSamples() {
        ParticleAppearanceParameterGenerator generator =
                new ParticleAppearanceParameterGenerator();
        ParticleAppearanceDistributionConfig distribution =
                ParticleAppearanceDistributionConfig.defaultConfig();
        ParticleAppearanceSample first = generator.generate(
                PsfShapeType.ELLIPTICAL_GAUSSIAN,
                PsfSizeClass.LARGE,
                composition(),
                distribution,
                77L,
                88L
        );
        ParticleAppearanceSample second = generator.generate(
                PsfShapeType.ELLIPTICAL_GAUSSIAN,
                PsfSizeClass.LARGE,
                composition(),
                distribution,
                77L,
                88L
        );

        assertEquals(first.getBrightnessWeight(),
                second.getBrightnessWeight(), 0.0);
        assertEquals(first.getPsfParameters().getSigmaXPixels(),
                second.getPsfParameters().getSigmaXPixels(), 0.0);
        assertEquals(first.getPsfParameters().getSigmaYPixels(),
                second.getPsfParameters().getSigmaYPixels(), 0.0);
        assertEquals(first.getPsfParameters().getRotationRadians(),
                second.getPsfParameters().getRotationRadians(), 0.0);
    }

    @Test
    public void everyCircularSampleRemainsInsideItsAssignedBand() {
        ParticleAppearanceParameterGenerator generator =
                new ParticleAppearanceParameterGenerator();
        ParticleAppearanceDistributionConfig distribution =
                ParticleAppearanceDistributionConfig.defaultConfig();
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            PsfSizeMixtureConfig.NumericRange range = distribution
                    .getPsfSizeMixtureConfig().getSigmaRangePixels(sizeClass);
            for (long seed = 1L; seed <= 100L; seed++) {
                ParticlePsfParameters psf = generator.generate(
                        PsfShapeType.CIRCULAR_GAUSSIAN,
                        sizeClass,
                        composition(),
                        distribution,
                        seed,
                        seed + 1000L
                ).getPsfParameters();
                assertEquals(sizeClass, psf.getSizeClass());
                assertTrue(psf.getSigmaXPixels() >= range.getMinimum());
                assertTrue(psf.getSigmaXPixels() <= range.getMaximum());
                assertEquals(psf.getSigmaXPixels(), psf.getSigmaYPixels(), 0.0);
            }
        }
    }

    @Test
    public void ellipticalMajorSigmaUsesSizeBandAndNeverExceedsTwoPointFive() {
        ParticleAppearanceParameterGenerator generator =
                new ParticleAppearanceParameterGenerator();
        ParticleAppearanceDistributionConfig distribution =
                ParticleAppearanceDistributionConfig.defaultConfig();
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            PsfSizeMixtureConfig.NumericRange range = distribution
                    .getPsfSizeMixtureConfig().getSigmaRangePixels(sizeClass);
            for (long seed = 1L; seed <= 100L; seed++) {
                ParticlePsfParameters psf = generator.generate(
                        PsfShapeType.ELLIPTICAL_GAUSSIAN,
                        sizeClass,
                        composition(),
                        distribution,
                        seed,
                        seed + 2000L
                ).getPsfParameters();
                assertTrue(psf.getSigmaXPixels() >= range.getMinimum());
                assertTrue(psf.getSigmaXPixels() <= range.getMaximum());
                assertTrue(psf.getSigmaXPixels() <= 2.5);
                double aspect = psf.getSigmaYPixels()
                        / psf.getSigmaXPixels();
                assertTrue(aspect >= 0.60);
                assertTrue(aspect <= 0.70);
                assertTrue(psf.getRotationRadians() >= 0.0);
                assertTrue(psf.getRotationRadians() < Math.PI);
            }
        }
    }

    @Test
    public void brightnessSeedDoesNotChangePsfGeometry() {
        ParticleAppearanceParameterGenerator generator =
                new ParticleAppearanceParameterGenerator();
        ParticleAppearanceDistributionConfig distribution =
                ParticleAppearanceDistributionConfig.defaultConfig();
        ParticleAppearanceSample first = generator.generate(
                PsfShapeType.ELLIPTICAL_GAUSSIAN,
                PsfSizeClass.MEDIUM,
                composition(),
                distribution,
                123L,
                456L
        );
        ParticleAppearanceSample second = generator.generate(
                PsfShapeType.ELLIPTICAL_GAUSSIAN,
                PsfSizeClass.MEDIUM,
                composition(),
                distribution,
                123L,
                789L
        );
        assertEquals(first.getPsfParameters().getSigmaXPixels(),
                second.getPsfParameters().getSigmaXPixels(), 0.0);
        assertEquals(first.getPsfParameters().getSigmaYPixels(),
                second.getPsfParameters().getSigmaYPixels(), 0.0);
        assertEquals(first.getPsfParameters().getRotationRadians(),
                second.getPsfParameters().getRotationRadians(), 0.0);
        assertTrue(first.getBrightnessWeight()
                != second.getBrightnessWeight());
    }

    @Test
    public void gaussianCopulaProducesRequestedWeakCorrelation() {
        ParticleAppearanceParameterGenerator generator =
                new ParticleAppearanceParameterGenerator();
        ParticleAppearanceDistributionConfig distribution =
                ParticleAppearanceDistributionConfig.defaultConfig();
        PsfSizeComposition composition = composition();
        NormalDistribution normal = new NormalDistribution(0.0, 1.0);
        CorrelationAccumulator values = new CorrelationAccumulator();
        int sampleCount = 6000;
        for (int index = 0; index < sampleCount; index++) {
            PsfSizeClass sizeClass = classForIndex(index, sampleCount);
            ParticleAppearanceSample sample = generator.generate(
                    PsfShapeType.CIRCULAR_GAUSSIAN,
                    sizeClass,
                    composition,
                    distribution,
                    SeedDerivation.psfGeometrySeed(998877L, index + 1),
                    SeedDerivation.brightnessSeed(998877L, index + 1)
            );
            PsfSizeMixtureConfig.NumericRange range = distribution
                    .getPsfSizeMixtureConfig().getSigmaRangePixels(sizeClass);
            double withinClass = (sample.getPsfParameters().getSigmaXPixels()
                    - range.getMinimum())
                    / (range.getMaximum() - range.getMinimum());
            double globalSizeQuantile = lowerCumulative(
                    sizeClass,
                    composition
            ) + composition.getRatio(sizeClass) * withinClass;
            double brightnessQuantile = Math.log(
                    sample.getBrightnessWeight()
                            / distribution.getBrightnessWeightMin()
            ) / Math.log(distribution.getBrightnessWeightMax()
                    / distribution.getBrightnessWeightMin());
            values.add(
                    normal.inverseCumulativeProbability(globalSizeQuantile),
                    normal.inverseCumulativeProbability(brightnessQuantile)
            );
        }
        assertEquals(0.25, values.correlation(), 0.04);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOverlappingSizeBands() {
        checkLargeBand(1.99, 2.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsSigmaAboveTwoPointFive() {
        checkLargeBand(2.0, 2.51);
    }

    private void checkLargeBand(double minimum, double maximum) {
        PsfSizeMixtureConfig defaults = PsfSizeMixtureConfig.defaultConfig();
        Map<PsfSizeClass, PsfSizeMixtureConfig.NumericRange> ranges =
                new EnumMap<PsfSizeClass, PsfSizeMixtureConfig.NumericRange>(defaults.getSigmaRangesPixels());
        ranges.put(PsfSizeClass.LARGE, new PsfSizeMixtureConfig.NumericRange(minimum, maximum));
        new PsfSizeMixtureConfig(ranges, defaults.getDirichletAlpha(), defaults.getProportionBounds());
    }

    private void assertRange(
            PsfSizeMixtureConfig mixture,
            PsfSizeClass sizeClass,
            double minimum,
            double maximum
    ) {
        assertEquals(minimum,
                mixture.getSigmaRangePixels(sizeClass).getMinimum(), 0.0);
        assertEquals(maximum,
                mixture.getSigmaRangePixels(sizeClass).getMaximum(), 0.0);
    }

    private PsfSizeComposition composition() {
        Map<PsfSizeClass, Double> ratios =
                new EnumMap<PsfSizeClass, Double>(PsfSizeClass.class);
        ratios.put(PsfSizeClass.SMALL, 0.30);
        ratios.put(PsfSizeClass.MEDIUM, 0.50);
        ratios.put(PsfSizeClass.LARGE, 0.20);
        return new PsfSizeComposition(ratios);
    }

    private PsfSizeClass classForIndex(int index, int sampleCount) {
        if (index < (int) (0.30 * sampleCount)) {
            return PsfSizeClass.SMALL;
        }
        if (index < (int) (0.80 * sampleCount)) {
            return PsfSizeClass.MEDIUM;
        }
        return PsfSizeClass.LARGE;
    }

    private double lowerCumulative(
            PsfSizeClass target,
            PsfSizeComposition composition
    ) {
        double cumulative = 0.0;
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            if (sizeClass == target) {
                break;
            }
            cumulative += composition.getRatio(sizeClass);
        }
        return cumulative;
    }

    private static final class CorrelationAccumulator {

        private double sumX;
        private double sumY;
        private double sumXX;
        private double sumYY;
        private double sumXY;
        private int count;

        private void add(double x, double y) {
            sumX += x;
            sumY += y;
            sumXX += x * x;
            sumYY += y * y;
            sumXY += x * y;
            count++;
        }

        private double correlation() {
            double covariance = count * sumXY - sumX * sumY;
            double varianceX = count * sumXX - sumX * sumX;
            double varianceY = count * sumYY - sumY * sumY;
            return covariance / Math.sqrt(varianceX * varianceY);
        }
    }
}
