package io.github.zhengfangfang0304.synspt.noise;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CameraFrameTest {

    @Test
    public void storesUnsignedSixteenBitValuesAndCopiesPixels() {
        short[] pixels = new short[] {0, 100, (short) 65535};
        CameraFrame frame = new CameraFrame(3, 1, 16, pixels);
        pixels[1] = 0;
        short[] returned = frame.copyPixels();
        returned[0] = 12;

        assertEquals(100, frame.getUnsignedAdu(1, 0));
        assertEquals(65535, frame.getUnsignedAdu(2, 0));
        assertArrayEquals(
                new short[] {0, 100, (short) 65535},
                frame.copyPixels()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsValuesOutsideEightBitRange() {
        new CameraFrame(1, 1, 8, new short[] {256});
    }
}
