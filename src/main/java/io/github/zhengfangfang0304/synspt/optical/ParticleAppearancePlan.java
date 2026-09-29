package io.github.zhengfangfang0304.synspt.optical;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Immutable realized PSF-size composition and per-particle appearance samples. */
public final class ParticleAppearancePlan {

    private final PsfSizeComposition sizeComposition;
    private final List<ParticleAppearanceSample> samples;
    private final Map<PsfSizeClass, Integer> realizedCounts;

    public ParticleAppearancePlan(
            PsfSizeComposition sizeComposition,
            List<ParticleAppearanceSample> samples
    ) {
        if (sizeComposition == null || samples == null || samples.isEmpty()) {
            throw new IllegalArgumentException(
                    "Appearance composition and non-empty samples are required."
            );
        }
        EnumMap<PsfSizeClass, Integer> counts =
                new EnumMap<PsfSizeClass, Integer>(PsfSizeClass.class);
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            counts.put(sizeClass, 0);
        }
        List<ParticleAppearanceSample> copy =
                new ArrayList<ParticleAppearanceSample>(samples.size());
        for (ParticleAppearanceSample sample : samples) {
            if (sample == null) {
                throw new IllegalArgumentException(
                        "Appearance samples cannot contain null."
                );
            }
            PsfSizeClass sizeClass = sample.getPsfParameters().getSizeClass();
            counts.put(sizeClass, counts.get(sizeClass).intValue() + 1);
            copy.add(sample);
        }
        this.sizeComposition = sizeComposition;
        this.samples = Collections.unmodifiableList(copy);
        this.realizedCounts = Collections.unmodifiableMap(counts);
    }

    public PsfSizeComposition getSizeComposition() {
        return sizeComposition;
    }

    public List<ParticleAppearanceSample> getSamples() {
        return samples;
    }

    public Map<PsfSizeClass, Integer> getRealizedCounts() {
        return realizedCounts;
    }
}
