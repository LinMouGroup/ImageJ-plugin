package io.github.zhengfangfang0304.synspt.export;

import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.*;
import org.junit.Test;
import java.util.Locale;
import static org.junit.Assert.assertEquals;

public class MotionParametersFormatterTest {
    @Test
    public void formatsAllEightRealParameterTypesWithUnits() {
        check(new BrownianParameters(1.0), "D=1.0 um^2/s");
        check(new DirectedParameters(0.1, 2.0, 1.57),
                "D=0.1 um^2/s; speed=2.0 um/s; direction=1.57 rad");
        check(new ConfinedParameters(0.2, 0.8, 20),
                "D=0.2 um^2/s; radius=0.8 um; substeps_per_frame=20; boundary=circular_rejecting_reflection");
        check(new FbmParameters(0.35, 0.4), "alpha=0.7; H=0.35; K_alpha=0.4 um^2/s^alpha");
        check(new CtrwParameters(0.6, 0.3, 0.01),
                "alpha=0.6; D=0.3 um^2/s; minimum_waiting_time=0.01 s");
        check(new LevyWalkParameters(1.5, 2.0, 0.02),
                "sigma=1.5; alpha=1.5; speed=2.0 um/s; minimum_flight_time=0.02 s");
        check(new SbmParameters(0.7, 0.4), "alpha=0.7; K_alpha=0.4 um^2/s^alpha");
        check(new AttmParameters(0.5, 0.5, 1.0, 0.3, 0.01),
                "alpha=0.5; sigma=0.5; gamma=1.0; D_max=0.3 um^2/s; minimum_dwell_time=0.01 s");
    }

    @Test
    public void formattingIsIndependentOfDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            check(new BrownianParameters(0.25), "D=0.25 um^2/s");
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMismatchedType() {
        MotionParametersFormatter.format(MotionType.DIRECTED, new BrownianParameters(1.0));
    }

    private void check(MotionParameters parameters, String expected) {
        assertEquals(expected, MotionParametersFormatter.format(parameters.getMotionType(), parameters));
    }
}
