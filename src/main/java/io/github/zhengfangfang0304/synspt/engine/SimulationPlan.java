package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.config.MotionCompositionConfig;
import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.noise.PhotonCalibrationResult;
import io.github.zhengfangfang0304.synspt.optical.PsfSizeComposition;
import io.github.zhengfangfang0304.synspt.optical.ParticleAppearancePlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable realized plan for one simulation run.
 *
 * <p>The request configuration records user choices, while this plan records
 * the concrete particle-level composition, motion parameters, imaging
 * parameters, and noise calibration that the engine will execute.</p>
 */
public final class SimulationPlan {

    private final SimulationConfig requestConfig;
    private final MotionCompositionConfig motionComposition;
    private final ParticleAppearancePlan appearancePlan;
    private final List<Particle> particles;
    private final PhotonCalibrationResult photonCalibration;

    public SimulationPlan(
            SimulationConfig requestConfig,
            MotionCompositionConfig motionComposition,
            ParticleAppearancePlan appearancePlan,
            List<Particle> particles,
            PhotonCalibrationResult photonCalibration
    ) {
        if (requestConfig == null
                || motionComposition == null
                || appearancePlan == null
                || particles == null
                || photonCalibration == null) {
            throw new IllegalArgumentException(
                    "Request config, motion composition, PSF-size composition, particles, and noise calibration cannot be null."
            );
        }
        if (particles.size() != requestConfig.getParticleNumber()) {
            throw new IllegalArgumentException(
                    "Plan particle count must match the request configuration."
            );
        }
        if (appearancePlan.getSamples().size() != particles.size()) {
            throw new IllegalArgumentException("Appearance sample count must match the plan particles.");
        }
        if (photonCalibration.getParticlePhotonBudgets().size()
                != particles.size()) {
            throw new IllegalArgumentException(
                    "Photon calibration count must match the plan particles."
            );
        }
        Map<MotionType, Double> ratios = motionComposition.getComposition();
        List<Particle> copy = new ArrayList<Particle>(particles.size());
        for (int index = 0; index < particles.size(); index++) {
            Particle particle = particles.get(index);
            if (particle == null) {
                throw new IllegalArgumentException("Plan particles cannot contain null.");
            }
            if (particle.getParticleId() != index + 1) {
                throw new IllegalArgumentException(
                        "Plan particles must have sequential one-based IDs."
                );
            }
            Double ratio = ratios.get(particle.getMotionType());
            if (ratio == null || ratio <= 0.0) {
                throw new IllegalArgumentException(
                        "Every particle motion type must occur in the composition."
                );
            }
            copy.add(particle);
        }
        this.requestConfig = requestConfig;
        this.motionComposition = motionComposition;
        this.appearancePlan = appearancePlan;
        this.particles = Collections.unmodifiableList(copy);
        this.photonCalibration = photonCalibration;
    }

    public SimulationConfig getRequestConfig() {
        return requestConfig;
    }

    public MotionCompositionConfig getMotionComposition() {
        return motionComposition;
    }

    public PsfSizeComposition getPsfSizeComposition() {
        return appearancePlan.getSizeComposition();
    }

    public ParticleAppearancePlan getAppearancePlan() {
        return appearancePlan;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public PhotonCalibrationResult getPhotonCalibration() {
        return photonCalibration;
    }
}
