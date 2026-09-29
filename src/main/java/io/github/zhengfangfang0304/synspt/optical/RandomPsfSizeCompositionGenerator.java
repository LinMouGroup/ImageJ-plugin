package io.github.zhengfangfang0304.synspt.optical;

import io.github.zhengfangfang0304.synspt.config.DirichletSampler;
import io.github.zhengfangfang0304.synspt.config.PsfSizeMixtureConfig;
import io.github.zhengfangfang0304.synspt.config.PsfSizeMixtureConfig.NumericRange;

import java.util.EnumMap;
import java.util.Random;

/** Generates one bounded Dirichlet PSF-size composition per simulation. */
public final class RandomPsfSizeCompositionGenerator {

    private static final int MAX_ATTEMPTS = 256;

    private final DirichletSampler dirichletSampler;

    public RandomPsfSizeCompositionGenerator() {
        this(new DirichletSampler());
    }

    public RandomPsfSizeCompositionGenerator(DirichletSampler dirichletSampler) {
        if (dirichletSampler == null) {
            throw new IllegalArgumentException(
                    "Dirichlet sampler cannot be null."
            );
        }
        this.dirichletSampler = dirichletSampler;
    }

    public PsfSizeComposition generate(
            PsfSizeMixtureConfig config,
            long compositionSeed
    ) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "PSF-size mixture config cannot be null."
            );
        }
        PsfSizeClass[] classes = PsfSizeClass.values();
        double[] alpha = new double[classes.length];
        for (int index = 0; index < classes.length; index++) {
            alpha[index] = config.getDirichletAlpha(classes[index]);
        }
        Random random = new Random(compositionSeed);
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            double[] sample = dirichletSampler.sample(alpha, random);
            if (withinBounds(sample, classes, config)) {
                return composition(sample, classes);
            }
        }
        return priorMean(alpha, classes);
    }

    private boolean withinBounds(
            double[] sample,
            PsfSizeClass[] classes,
            PsfSizeMixtureConfig config
    ) {
        for (int index = 0; index < classes.length; index++) {
            NumericRange bounds = config.getProportionBounds(classes[index]);
            if (sample[index] < bounds.getMinimum()
                    || sample[index] > bounds.getMaximum()) {
                return false;
            }
        }
        return true;
    }

    private PsfSizeComposition composition(
            double[] ratios,
            PsfSizeClass[] classes
    ) {
        EnumMap<PsfSizeClass, Double> values =
                new EnumMap<PsfSizeClass, Double>(PsfSizeClass.class);
        for (int index = 0; index < classes.length; index++) {
            values.put(classes[index], ratios[index]);
        }
        return new PsfSizeComposition(values);
    }

    private PsfSizeComposition priorMean(
            double[] alpha,
            PsfSizeClass[] classes
    ) {
        double sum = 0.0;
        for (double value : alpha) {
            sum += value;
        }
        double[] ratios = new double[alpha.length];
        for (int index = 0; index < alpha.length; index++) {
            ratios[index] = alpha[index] / sum;
        }
        return composition(ratios, classes);
    }
}
