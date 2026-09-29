package io.github.zhengfangfang0304.synspt.optical;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParticlePsfParametersTest {

    @Test
    public void retainsGroundTruthSizeClass() {
        ParticlePsfParameters parameters = new ParticlePsfParameters(
                PsfSizeClass.LARGE,
                PsfShapeType.CIRCULAR_GAUSSIAN,
                11.0,
                2.2,
                2.2,
                0.0
        );
        assertEquals(PsfSizeClass.LARGE, parameters.getSizeClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingSizeClass() {
        new ParticlePsfParameters(
                null,
                PsfShapeType.CIRCULAR_GAUSSIAN,
                7.5,
                1.5,
                1.5,
                0.0
        );
    }
}
