package io.github.zhengfangfang0304.synspt.optical;

import org.junit.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PsfSizeClassAssignerTest {

    @Test
    public void assignsExactCountsAndAtLeastOneOfEachClass() {
        List<PsfSizeClass> assignments = new PsfSizeClassAssigner().assign(
                composition(0.30, 0.50, 0.20),
                100,
                77L
        );
        assertEquals(100, assignments.size());
        assertEquals(30, count(assignments, PsfSizeClass.SMALL));
        assertEquals(50, count(assignments, PsfSizeClass.MEDIUM));
        assertEquals(20, count(assignments, PsfSizeClass.LARGE));
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            assertTrue(count(assignments, sizeClass) > 0);
        }
    }

    @Test
    public void fixedSeedReproducesAssignmentOrder() {
        PsfSizeClassAssigner assigner = new PsfSizeClassAssigner();
        PsfSizeComposition composition = composition(0.27, 0.51, 0.22);
        assertEquals(
                assigner.assign(composition, 53, 998L),
                assigner.assign(composition, 53, 998L)
        );
    }

    private int count(List<PsfSizeClass> values, PsfSizeClass target) {
        int count = 0;
        for (PsfSizeClass value : values) {
            if (value == target) {
                count++;
            }
        }
        return count;
    }

    private PsfSizeComposition composition(
            double small,
            double medium,
            double large
    ) {
        Map<PsfSizeClass, Double> ratios =
                new EnumMap<PsfSizeClass, Double>(PsfSizeClass.class);
        ratios.put(PsfSizeClass.SMALL, small);
        ratios.put(PsfSizeClass.MEDIUM, medium);
        ratios.put(PsfSizeClass.LARGE, large);
        return new PsfSizeComposition(ratios);
    }
}
