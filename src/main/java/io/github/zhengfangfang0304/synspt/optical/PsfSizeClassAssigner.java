package io.github.zhengfangfang0304.synspt.optical;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Converts PSF-size ratios to exact particle counts using largest remainder. */
public final class PsfSizeClassAssigner {

    public List<PsfSizeClass> assign(
            PsfSizeComposition composition,
            int particleCount,
            long assignmentSeed
    ) {
        if (composition == null) {
            throw new IllegalArgumentException(
                    "PSF-size composition cannot be null."
            );
        }
        if (particleCount <= 0) {
            throw new IllegalArgumentException(
                    "Particle count must be positive."
            );
        }

        EnumMap<PsfSizeClass, Integer> counts =
                new EnumMap<PsfSizeClass, Integer>(PsfSizeClass.class);
        int guaranteed = particleCount >= PsfSizeClass.values().length ? 1 : 0;
        int remaining = particleCount
                - guaranteed * PsfSizeClass.values().length;
        List<Remainder> remainders = new ArrayList<Remainder>();
        int assigned = guaranteed * PsfSizeClass.values().length;
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            double quota = remaining * composition.getRatio(sizeClass);
            int floor = (int) Math.floor(quota);
            counts.put(sizeClass, guaranteed + floor);
            assigned += floor;
            remainders.add(new Remainder(sizeClass, quota - floor));
        }
        Collections.sort(remainders, new Comparator<Remainder>() {
            @Override
            public int compare(Remainder left, Remainder right) {
                int byRemainder = Double.compare(
                        right.fraction,
                        left.fraction
                );
                return byRemainder != 0
                        ? byRemainder
                        : left.sizeClass.compareTo(right.sizeClass);
            }
        });
        for (int index = 0; assigned < particleCount; index++, assigned++) {
            PsfSizeClass sizeClass = remainders.get(index).sizeClass;
            counts.put(sizeClass, counts.get(sizeClass).intValue() + 1);
        }

        List<PsfSizeClass> assignments =
                new ArrayList<PsfSizeClass>(particleCount);
        for (Map.Entry<PsfSizeClass, Integer> entry : counts.entrySet()) {
            for (int index = 0; index < entry.getValue().intValue(); index++) {
                assignments.add(entry.getKey());
            }
        }
        Collections.shuffle(assignments, new Random(assignmentSeed));
        return Collections.unmodifiableList(assignments);
    }

    private static final class Remainder {

        private final PsfSizeClass sizeClass;
        private final double fraction;

        private Remainder(PsfSizeClass sizeClass, double fraction) {
            this.sizeClass = sizeClass;
            this.fraction = fraction;
        }
    }
}
