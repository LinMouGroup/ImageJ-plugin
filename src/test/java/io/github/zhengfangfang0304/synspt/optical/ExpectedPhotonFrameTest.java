package io.github.zhengfangfang0304.synspt.optical;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ExpectedPhotonFrameTest {

    @Test
    public void defensivelyCopiesExpectedPhotonValues() {
        double[] pixels = new double[] {1.0, 2.0, 3.0, 4.0};
        ExpectedPhotonFrame frame = new ExpectedPhotonFrame(2, 2, pixels);
        pixels[0] = 99.0;
        double[] returned = frame.copyExpectedPhotons();
        returned[1] = 99.0;

        assertEquals(1.0, frame.getExpectedPhotons(0, 0), 0.0);
        assertEquals(4.0, frame.getExpectedPhotons(1, 1), 0.0);
        assertArrayEquals(
                new double[] {1.0, 2.0, 3.0, 4.0},
                frame.copyExpectedPhotons(),
                0.0
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeExpectedPhotons() {
        new ExpectedPhotonFrame(1, 1, new double[] {-0.1});
    }
}
