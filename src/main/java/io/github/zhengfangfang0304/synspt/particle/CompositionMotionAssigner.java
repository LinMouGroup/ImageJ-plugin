package io.github.zhengfangfang0304.synspt.particle;

import io.github.zhengfangfang0304.synspt.config.MotionCompositionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Converts normalized motion ratios into an exact, reproducible particle-level
 * assignment using the largest-remainder method.
 */
public final class CompositionMotionAssigner {

    public List<MotionType> assign(
            MotionCompositionConfig compositionConfig,
            int particleCount,
            long assignmentSeed
    ) {
        if (compositionConfig == null) {
            throw new IllegalArgumentException(
                    "Motion composition cannot be null."
            );
        }
        if (particleCount <= 0) {
            throw new IllegalArgumentException("Particle count must be positive.");
        }
        Map<MotionType, Double> composition =
                compositionConfig.getComposition();
        if (particleCount < composition.size()) {
            throw new IllegalArgumentException(
                    "Particle count must be at least the number of motion models."
            );
        }

        EnumMap<MotionType, Integer> counts =
                new EnumMap<MotionType, Integer>(MotionType.class);
        List<Remainder> remainders = new ArrayList<Remainder>(
                composition.size()
        );
        int distributable = particleCount - composition.size();
        int allocated = 0;
        for (Map.Entry<MotionType, Double> entry : composition.entrySet()) {
            double quota = distributable * entry.getValue();
            int floor = (int) Math.floor(quota);
            counts.put(entry.getKey(), 1 + floor);
            allocated += floor;
            remainders.add(new Remainder(
                    entry.getKey(),
                    quota - floor
            ));
        }

        Collections.sort(remainders, new Comparator<Remainder>() {
            @Override
            public int compare(Remainder left, Remainder right) {
                int byRemainder = Double.compare(
                        right.fraction,
                        left.fraction
                );
                if (byRemainder != 0) {
                    return byRemainder;
                }
                return Integer.compare(
                        left.motionType.ordinal(),
                        right.motionType.ordinal()
                );
            }
        });
        int unallocated = distributable - allocated;
        for (int index = 0; index < unallocated; index++) {
            MotionType type = remainders.get(index).motionType;
            counts.put(type, counts.get(type) + 1);
        }

        List<MotionType> assignments = new ArrayList<MotionType>(particleCount);
        for (MotionType type : composition.keySet()) {
            for (int index = 0; index < counts.get(type); index++) {
                assignments.add(type);
            }
        }
        Collections.shuffle(assignments, new Random(assignmentSeed));
        return Collections.unmodifiableList(assignments);
    }

    private static final class Remainder {

        private final MotionType motionType;
        private final double fraction;

        private Remainder(MotionType motionType, double fraction) {
            this.motionType = motionType;
            this.fraction = fraction;
        }
    }
}
