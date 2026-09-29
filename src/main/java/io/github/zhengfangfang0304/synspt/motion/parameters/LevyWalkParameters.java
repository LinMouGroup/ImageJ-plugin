package io.github.zhengfangfang0304.synspt.motion.parameters;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Map;

/** Parameters for a two-dimensional Lévy walk. */
public final class LevyWalkParameters implements MotionParameters {

    private final double flightTimeExponentSigma;
    private final double speedUmPerSecond;
    private final double minimumFlightTimeSeconds;

    public LevyWalkParameters(
            double flightTimeExponentSigma,
            double speedUmPerSecond,
            double minimumFlightTimeSeconds
    ) {
        if (!ParameterSupport.isFinite(flightTimeExponentSigma)
                || flightTimeExponentSigma <= 1.0
                || flightTimeExponentSigma >= 2.0) {
            throw new IllegalArgumentException(
                    "Lévy walk sigma must be strictly between one and two."
            );
        }
        ParameterSupport.requirePositive(speedUmPerSecond, "Lévy walk speed");
        ParameterSupport.requirePositive(
                minimumFlightTimeSeconds,
                "Minimum flight time"
        );
        this.flightTimeExponentSigma = flightTimeExponentSigma;
        this.speedUmPerSecond = speedUmPerSecond;
        this.minimumFlightTimeSeconds = minimumFlightTimeSeconds;
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.LEVY_WALK;
    }

    public double getFlightTimeExponentSigma() {
        return flightTimeExponentSigma;
    }

    public double getAlpha() {
        return 3.0 - flightTimeExponentSigma;
    }

    public double getSpeedUmPerSecond() {
        return speedUmPerSecond;
    }

    public double getMinimumFlightTimeSeconds() {
        return minimumFlightTimeSeconds;
    }

    @Override
    public Map<String, String> toMetadata() {
        return ParameterSupport.metadata(
                "sigma", ParameterSupport.number(flightTimeExponentSigma),
                "alpha", ParameterSupport.number(getAlpha()),
                "speed_um_per_s", ParameterSupport.number(speedUmPerSecond),
                "tau0_s", ParameterSupport.number(minimumFlightTimeSeconds)
        );
    }
}
