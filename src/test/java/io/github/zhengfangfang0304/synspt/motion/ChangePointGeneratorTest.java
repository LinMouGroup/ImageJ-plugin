package io.github.zhengfangfang0304.synspt.motion;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ChangePointGeneratorTest {

    @Test
    public void createsRandomCompletePartitionsWithTheNormalMinimum() {
        ChangePointGenerator generator = new ChangePointGenerator();
        List<Integer> durations = generator.generateDurations(100, 4, 91L);

        assertEquals(4, durations.size());
        assertEquals(100, sum(durations));
        for (Integer duration : durations) {
            assertTrue(duration >= 10);
        }
        assertNotEquals(durations.get(0), durations.get(1));
    }

    @Test
    public void automaticallyReducesTheMinimumForShortTrajectories() {
        List<Integer> durations = new ChangePointGenerator()
                .generateDurations(7, 3, 22L);

        assertEquals(7, sum(durations));
        for (Integer duration : durations) {
            assertTrue(duration >= 2);
        }
    }

    @Test
    public void fixedSeedIsExactlyReproducible() {
        ChangePointGenerator generator = new ChangePointGenerator();
        assertEquals(
                generator.generateDurations(87, 5, 123L),
                generator.generateDurations(87, 5, 123L)
        );
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnedDurationsCannotBeModified() {
        new ChangePointGenerator().generateDurations(40, 2, 1L).clear();
    }

    private int sum(List<Integer> values) {
        int result = 0;
        for (Integer value : values) {
            result += value;
        }
        return result;
    }
}
