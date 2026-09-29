package io.github.zhengfangfang0304.synspt.motion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** Generates random, complete trajectory partitions with no equal-split rule. */
public final class ChangePointGenerator {

    public static final int DEFAULT_MINIMUM_SEGMENT_LENGTH_FRAMES = 10;

    /**
     * Returns positive segment durations whose sum is exactly {@code frameCount}.
     * The normal minimum is ten frames and is reduced automatically for short
     * trajectories that cannot satisfy that minimum for every state.
     */
    public List<Integer> generateDurations(
            int frameCount,
            int stateCount,
            long randomSeed
    ) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("Frame count must be positive.");
        }
        if (stateCount < 2 || stateCount > frameCount) {
            throw new IllegalArgumentException(
                    "State count must be between two and the frame count."
            );
        }

        int minimumLength = Math.min(
                DEFAULT_MINIMUM_SEGMENT_LENGTH_FRAMES,
                frameCount / stateCount
        );
        int distributableFrames = frameCount - minimumLength * stateCount;
        int[] extras = dirichletLikeAllocation(
                distributableFrames,
                stateCount,
                new Random(randomSeed)
        );
        List<Integer> durations = new ArrayList<Integer>(stateCount);
        for (int stateIndex = 0; stateIndex < stateCount; stateIndex++) {
            durations.add(minimumLength + extras[stateIndex]);
        }
        return Collections.unmodifiableList(durations);
    }

    private int[] dirichletLikeAllocation(
            int total,
            int count,
            Random random
    ) {
        int[] allocations = new int[count];
        if (total == 0) {
            return allocations;
        }

        double[] weights = new double[count];
        double weightSum = 0.0;
        for (int index = 0; index < count; index++) {
            weights[index] = -Math.log(1.0 - random.nextDouble());
            weightSum += weights[index];
        }
        if (weightSum == 0.0) {
            allocations[0] = total;
            return allocations;
        }

        List<Remainder> remainders = new ArrayList<Remainder>(count);
        int allocated = 0;
        for (int index = 0; index < count; index++) {
            double exact = total * weights[index] / weightSum;
            int floor = (int) Math.floor(exact);
            allocations[index] = floor;
            allocated += floor;
            remainders.add(new Remainder(index, exact - floor));
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
                        : Integer.compare(left.index, right.index);
            }
        });
        for (int index = 0; index < total - allocated; index++) {
            allocations[remainders.get(index).index]++;
        }
        return allocations;
    }

    private static final class Remainder {
        private final int index;
        private final double fraction;

        private Remainder(int index, double fraction) {
            this.index = index;
            this.fraction = fraction;
        }
    }
}
