package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.util.SeedDerivation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Creates the independently sampled state plan for one composite trajectory. */
public final class MultiStateGenerator {

    public static final int MINIMUM_STATE_COUNT = 2;
    public static final int MAXIMUM_STATE_COUNT = 5;
    private static final double CONSECUTIVE_REPEAT_WEIGHT = 0.25;

    private final ChangePointGenerator changePointGenerator;
    private final StateParameterGenerator stateParameterGenerator;

    public MultiStateGenerator() {
        this(new ChangePointGenerator(), new StateParameterGenerator());
    }

    public MultiStateGenerator(
            ChangePointGenerator changePointGenerator,
            StateParameterGenerator stateParameterGenerator
    ) {
        if (changePointGenerator == null || stateParameterGenerator == null) {
            throw new IllegalArgumentException(
                    "Multi-state generator dependencies cannot be null."
            );
        }
        this.changePointGenerator = changePointGenerator;
        this.stateParameterGenerator = stateParameterGenerator;
    }

    public List<StateSegment> generate(
            SimulationConfig config,
            int particleId
    ) {
        if (config == null) {
            throw new IllegalArgumentException("Simulation config cannot be null.");
        }
        if (particleId <= 0) {
            throw new IllegalArgumentException("Particle ID must be positive.");
        }
        if (config.getFrames() < MINIMUM_STATE_COUNT) {
            throw new IllegalArgumentException(
                    "Multi-state motion requires at least two frames."
            );
        }

        long rootSeed = config.getRandomSeed();
        int stateCount = sampleStateCount(
                config.getFrames(),
                SeedDerivation.multiStateCountSeed(rootSeed, particleId)
        );
        List<Integer> durations = changePointGenerator.generateDurations(
                config.getFrames(),
                stateCount,
                SeedDerivation.multiStateChangePointSeed(rootSeed, particleId)
        );
        List<MotionType> stateTypes = sampleStateTypes(
                stateCount,
                SeedDerivation.multiStateTypeSeed(rootSeed, particleId)
        );

        List<StateSegment> segments = new ArrayList<StateSegment>(stateCount);
        int startFrame = 0;
        for (int stateIndex = 0; stateIndex < stateCount; stateIndex++) {
            int endFrame = startFrame + durations.get(stateIndex) - 1;
            MotionType type = stateTypes.get(stateIndex);
            MotionParameters parameters = stateParameterGenerator.generate(
                    type,
                    config.getMotionParameterDistributionConfig(),
                    config.getMicroscopeConfig().getFrameIntervalSeconds(),
                    rootSeed,
                    particleId,
                    stateIndex
            );
            segments.add(new StateSegment(
                    particleId,
                    stateIndex,
                    startFrame,
                    endFrame,
                    type,
                    parameters
            ));
            startFrame = endFrame + 1;
        }
        return Collections.unmodifiableList(segments);
    }

    private int sampleStateCount(int frameCount, long seed) {
        int normalMaximum = Math.max(
                MINIMUM_STATE_COUNT,
                frameCount
                        / ChangePointGenerator.DEFAULT_MINIMUM_SEGMENT_LENGTH_FRAMES
        );
        int maximum = Math.min(
                MAXIMUM_STATE_COUNT,
                Math.min(frameCount, normalMaximum)
        );
        return MINIMUM_STATE_COUNT + new Random(seed).nextInt(
                maximum - MINIMUM_STATE_COUNT + 1
        );
    }

    private List<MotionType> sampleStateTypes(int stateCount, long seed) {
        MotionType[] baseTypes = MotionType.baseTypes().toArray(
                new MotionType[MotionType.baseTypes().size()]
        );
        Random random = new Random(seed);
        List<MotionType> stateTypes = new ArrayList<MotionType>(stateCount);
        MotionType previous = null;
        for (int stateIndex = 0; stateIndex < stateCount; stateIndex++) {
            double totalWeight = baseTypes.length;
            if (previous != null) {
                totalWeight -= 1.0 - CONSECUTIVE_REPEAT_WEIGHT;
            }
            double draw = random.nextDouble() * totalWeight;
            MotionType selected = baseTypes[baseTypes.length - 1];
            for (MotionType candidate : baseTypes) {
                double weight = candidate == previous
                        ? CONSECUTIVE_REPEAT_WEIGHT
                        : 1.0;
                if (draw < weight) {
                    selected = candidate;
                    break;
                }
                draw -= weight;
            }
            stateTypes.add(selected);
            previous = selected;
        }
        return Collections.unmodifiableList(stateTypes);
    }
}
