package io.github.zhengfangfang0304.synspt.optical;

import io.github.zhengfangfang0304.synspt.config.ParticleAppearanceDistributionConfig;
import io.github.zhengfangfang0304.synspt.config.PsfSizeMixtureConfig.NumericRange;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Random;

/**
 * Samples one lifetime-stable set of appearance parameters per particle.
 *
 * <p>A Gaussian copula introduces only a weak positive association between
 * PSF size and brightness. Brightness never changes the sampled PSF geometry,
 * and the correlation does not compensate the sigma-squared peak loss.</p>
 */
public final class ParticleAppearanceParameterGenerator {

    private static final NormalDistribution STANDARD_NORMAL =
            new NormalDistribution(0.0, 1.0);
    private static final double OPEN_QUANTILE_EPSILON = 1.0e-12;

    public ParticleAppearanceSample generate(
            PsfShapeType shapeType,
            PsfSizeClass sizeClass,
            PsfSizeComposition sizeComposition,
            ParticleAppearanceDistributionConfig distribution,
            long geometrySeed,
            long brightnessSeed
    ) {
        if (shapeType == null || sizeClass == null
                || sizeComposition == null || distribution == null) {
            throw new IllegalArgumentException(
                    "PSF shape, size class, composition, and distribution cannot be null."
            );
        }
        Random geometryRandom = new Random(geometrySeed);
        double sizeQuantile = geometryRandom.nextDouble();
        double aspectRatioQuantile = geometryRandom.nextDouble();
        double rotationQuantile = geometryRandom.nextDouble();

        NumericRange sigmaRange = distribution.getPsfSizeMixtureConfig()
                .getSigmaRangePixels(sizeClass);
        double sizeSigma = uniform(
                sizeQuantile,
                sigmaRange.getMinimum(),
                sigmaRange.getMaximum()
        );
        ParticlePsfParameters psf;
        if (shapeType == PsfShapeType.CIRCULAR_GAUSSIAN) {
            psf = new ParticlePsfParameters(
                    sizeClass,
                    PsfShapeType.CIRCULAR_GAUSSIAN,
                    distribution.getPsfCutoffSigma() * sizeSigma,
                    sizeSigma,
                    sizeSigma,
                    0.0
            );
        } else if (shapeType == PsfShapeType.ELLIPTICAL_GAUSSIAN) {
            double aspectRatio = uniform(
                    aspectRatioQuantile,
                    distribution.getEllipticalAspectRatioMin(),
                    distribution.getEllipticalAspectRatioMax()
            );
            psf = new ParticlePsfParameters(
                    sizeClass,
                    PsfShapeType.ELLIPTICAL_GAUSSIAN,
                    distribution.getPsfCutoffSigma() * sizeSigma,
                    sizeSigma,
                    sizeSigma * aspectRatio,
                    rotationQuantile * Math.PI
            );
        } else {
            throw new IllegalArgumentException(
                    "Unsupported PSF shape: " + shapeType.name()
            );
        }

        double brightnessQuantile = correlatedBrightnessQuantile(
                sizeClass,
                sizeQuantile,
                sizeComposition,
                distribution.getSizeBrightnessCorrelation(),
                new Random(brightnessSeed)
        );
        double brightnessWeight = logUniform(
                brightnessQuantile,
                distribution.getBrightnessWeightMin(),
                distribution.getBrightnessWeightMax()
        );
        return new ParticleAppearanceSample(psf, brightnessWeight);
    }

    private double correlatedBrightnessQuantile(
            PsfSizeClass sizeClass,
            double withinClassQuantile,
            PsfSizeComposition composition,
            double correlation,
            Random random
    ) {
        double lowerCumulative = 0.0;
        for (PsfSizeClass candidate : PsfSizeClass.values()) {
            if (candidate == sizeClass) {
                break;
            }
            lowerCumulative += composition.getRatio(candidate);
        }
        double sizeQuantile = lowerCumulative
                + composition.getRatio(sizeClass) * withinClassQuantile;
        sizeQuantile = openQuantile(sizeQuantile);
        double sizeNormal = STANDARD_NORMAL.inverseCumulativeProbability(
                sizeQuantile
        );
        double brightnessNormal = correlation * sizeNormal
                + Math.sqrt(1.0 - correlation * correlation)
                * random.nextGaussian();
        return openQuantile(
                STANDARD_NORMAL.cumulativeProbability(brightnessNormal)
        );
    }

    private double openQuantile(double quantile) {
        return Math.max(
                OPEN_QUANTILE_EPSILON,
                Math.min(1.0 - OPEN_QUANTILE_EPSILON, quantile)
        );
    }

    private double uniform(double quantile, double minimum, double maximum) {
        return minimum + quantile * (maximum - minimum);
    }

    private double logUniform(double quantile, double minimum, double maximum) {
        double logMinimum = Math.log(minimum);
        return Math.exp(logMinimum
                + quantile * (Math.log(maximum) - logMinimum));
    }
}
