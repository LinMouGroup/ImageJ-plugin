package io.github.zhengfangfang0304.synspt.export;

import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.*;

/** Human-readable, locale-independent descriptions of realized model parameters. */
public final class MotionParametersFormatter {
    private MotionParametersFormatter() { }

    public static String format(MotionType type, MotionParameters parameters) {
        if (type == null || parameters == null || type != parameters.getMotionType()
                || type.isComposite()) {
            throw new IllegalArgumentException("A matching basic motion type and parameters are required.");
        }
        switch (type) {
            case BROWNIAN: {
                BrownianParameters p = (BrownianParameters) parameters;
                return diffusion(p.getDiffusionUm2PerSecond());
            }
            case DIRECTED: {
                DirectedParameters p = (DirectedParameters) parameters;
                return diffusion(p.getDiffusionUm2PerSecond())
                        + "; speed=" + p.getSpeedUmPerSecond() + " um/s"
                        + "; direction=" + p.getDirectionRadians() + " rad";
            }
            case CONFINED: {
                ConfinedParameters p = (ConfinedParameters) parameters;
                return diffusion(p.getDiffusionUm2PerSecond())
                        + "; radius=" + p.getRadiusUm() + " um"
                        + "; substeps_per_frame=" + p.getSubstepsPerFrame()
                        + "; boundary=circular_rejecting_reflection";
            }
            case FBM: {
                FbmParameters p = (FbmParameters) parameters;
                return "alpha=" + p.getAlpha() + "; H=" + p.getHurstExponent()
                        + generalizedDiffusion(p.getGeneralizedDiffusionUm2PerSecondAlpha());
            }
            case CTRW: {
                CtrwParameters p = (CtrwParameters) parameters;
                return "alpha=" + p.getAlpha() + "; " + diffusion(p.getDiffusionUm2PerSecond())
                        + "; minimum_waiting_time=" + p.getMinimumWaitingTimeSeconds() + " s";
            }
            case LEVY_WALK: {
                LevyWalkParameters p = (LevyWalkParameters) parameters;
                return "sigma=" + p.getFlightTimeExponentSigma() + "; alpha=" + p.getAlpha()
                        + "; speed=" + p.getSpeedUmPerSecond() + " um/s"
                        + "; minimum_flight_time=" + p.getMinimumFlightTimeSeconds() + " s";
            }
            case SBM: {
                SbmParameters p = (SbmParameters) parameters;
                return "alpha=" + p.getAlpha()
                        + generalizedDiffusion(p.getGeneralizedDiffusionUm2PerSecondAlpha());
            }
            case ATTM: {
                AttmParameters p = (AttmParameters) parameters;
                return "alpha=" + p.getAlpha() + "; sigma=" + p.getDiffusivityExponentSigma()
                        + "; gamma=" + p.getDwellExponentGamma()
                        + "; D_max=" + p.getMaximumDiffusionUm2PerSecond() + " um^2/s"
                        + "; minimum_dwell_time=" + p.getMinimumDwellTimeSeconds() + " s";
            }
            default:
                throw new IllegalArgumentException("Unsupported motion type: " + type);
        }
    }

    private static String diffusion(double value) {
        return "D=" + value + " um^2/s";
    }

    private static String generalizedDiffusion(double value) {
        return "; K_alpha=" + value + " um^2/s^alpha";
    }
}
