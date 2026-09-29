package io.github.zhengfangfang0304.synspt.particle;

import io.github.zhengfangfang0304.synspt.config.MotionCompositionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;

import org.junit.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CompositionMotionAssignerTest {

    @Test
    public void equalFourModelCompositionAssignsTwentyFiveEach() {
        List<MotionType> assignments = new CompositionMotionAssigner().assign(
                equalComposition(EnumSet.of(
                        MotionType.BROWNIAN,
                        MotionType.DIRECTED,
                        MotionType.FBM,
                        MotionType.CTRW
                )),
                100,
                11L
        );

        assertEquals(100, assignments.size());
        assertEquals(25, count(assignments, MotionType.BROWNIAN));
        assertEquals(25, count(assignments, MotionType.DIRECTED));
        assertEquals(25, count(assignments, MotionType.FBM));
        assertEquals(25, count(assignments, MotionType.CTRW));
    }

    @Test
    public void largestRemaindersProduceAnExactTotal() {
        MotionCompositionConfig composition = composition(
                MotionType.BROWNIAN, 0.50,
                MotionType.FBM, 0.30,
                MotionType.CTRW, 0.20
        );
        List<MotionType> assignments = new CompositionMotionAssigner().assign(
                composition,
                11,
                22L
        );

        assertEquals(11, assignments.size());
        assertEquals(5, count(assignments, MotionType.BROWNIAN));
        assertEquals(3, count(assignments, MotionType.FBM));
        assertEquals(3, count(assignments, MotionType.CTRW));
    }

    @Test
    public void fixedSeedIsReproducibleAndSeedOnlyChangesOrder() {
        MotionCompositionConfig composition = equalComposition(EnumSet.of(
                MotionType.BROWNIAN,
                MotionType.FBM,
                MotionType.CTRW
        ));
        CompositionMotionAssigner assigner = new CompositionMotionAssigner();
        List<MotionType> first = assigner.assign(composition, 50, 123L);
        List<MotionType> repeated = assigner.assign(composition, 50, 123L);
        List<MotionType> reordered = assigner.assign(composition, 50, 124L);

        assertEquals(first, repeated);
        for (MotionType type : composition.getComposition().keySet()) {
            assertEquals(count(first, type), count(reordered, type));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsParticleCountBelowSelectedModelCount() {
        new CompositionMotionAssigner().assign(
                equalComposition(EnumSet.of(
                        MotionType.BROWNIAN,
                        MotionType.FBM,
                        MotionType.CTRW
                )),
                2,
                1L
        );
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnsAnImmutableAssignmentList() {
        new CompositionMotionAssigner().assign(
                equalComposition(EnumSet.of(MotionType.BROWNIAN)),
                1,
                1L
        ).clear();
    }

    private MotionCompositionConfig equalComposition(
            EnumSet<MotionType> types
    ) {
        EnumMap<MotionType, Double> ratios =
                new EnumMap<MotionType, Double>(MotionType.class);
        double ratio = 1.0 / types.size();
        for (MotionType type : types) {
            ratios.put(type, ratio);
        }
        return new MotionCompositionConfig(ratios);
    }

    private MotionCompositionConfig composition(
            MotionType firstType,
            double firstRatio,
            MotionType secondType,
            double secondRatio,
            MotionType thirdType,
            double thirdRatio
    ) {
        EnumMap<MotionType, Double> ratios =
                new EnumMap<MotionType, Double>(MotionType.class);
        ratios.put(firstType, firstRatio);
        ratios.put(secondType, secondRatio);
        ratios.put(thirdType, thirdRatio);
        return new MotionCompositionConfig(ratios);
    }

    private int count(List<MotionType> assignments, MotionType expected) {
        int count = 0;
        for (MotionType assignment : assignments) {
            if (assignment == expected) {
                count++;
            }
        }
        return count;
    }
}
