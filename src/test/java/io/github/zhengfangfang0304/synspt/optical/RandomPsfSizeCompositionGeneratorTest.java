package io.github.zhengfangfang0304.synspt.optical;

import io.github.zhengfangfang0304.synspt.config.PsfSizeMixtureConfig;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomPsfSizeCompositionGeneratorTest {

    @Test
    public void fixedSeedIsReproducibleAndRatiosRespectHardBounds() {
        RandomPsfSizeCompositionGenerator generator =
                new RandomPsfSizeCompositionGenerator();
        PsfSizeMixtureConfig config = PsfSizeMixtureConfig.defaultConfig();
        PsfSizeComposition first = generator.generate(config, 12345L);
        PsfSizeComposition second = generator.generate(config, 12345L);

        assertEquals(first.getRatios(), second.getRatios());
        double sum = 0.0;
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            double ratio = first.getRatio(sizeClass);
            PsfSizeMixtureConfig.NumericRange bounds =
                    config.getProportionBounds(sizeClass);
            assertTrue(ratio >= bounds.getMinimum());
            assertTrue(ratio <= bounds.getMaximum());
            sum += ratio;
        }
        assertEquals(1.0, sum, 1.0e-12);
    }

    @Test
    public void differentSimulationSeedsProduceDifferentCompositions() {
        RandomPsfSizeCompositionGenerator generator =
                new RandomPsfSizeCompositionGenerator();
        Set<String> realized = new HashSet<String>();
        for (long seed = 1L; seed <= 20L; seed++) {
            realized.add(generator.generate(
                    PsfSizeMixtureConfig.defaultConfig(), seed
            ).getRatios().toString());
        }
        assertTrue(realized.size() > 1);
    }
}
