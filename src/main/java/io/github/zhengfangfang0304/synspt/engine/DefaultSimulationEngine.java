package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.config.SimulationConfigValidator;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;
import io.github.zhengfangfang0304.synspt.scene.ParticleTrackGenerator;
import io.github.zhengfangfang0304.synspt.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/** Default deterministic orchestration of the particle and scene layers. */
public final class DefaultSimulationEngine implements SimulationEngine {

    private static final int PARTICLE_DEFINITION_PROGRESS = 5;
    private static final int TRACK_GENERATION_PROGRESS_RANGE = 90;

    private final SimulationPlanFactory planFactory;
    private final ParticleTrackGenerator trackGenerator;

    public DefaultSimulationEngine() {
        this(new SimulationPlanFactory(), new ParticleTrackGenerator());
    }

    DefaultSimulationEngine(
            SimulationPlanFactory planFactory,
            ParticleTrackGenerator trackGenerator
    ) {
        if (planFactory == null || trackGenerator == null) {
            throw new IllegalArgumentException(
                    "Simulation plan factory and track generator cannot be null."
            );
        }
        this.planFactory = planFactory;
        this.trackGenerator = trackGenerator;
    }

    @Override
    public SimulationResult generate(
            SimulationConfig config,
            SimulationProgressListener progressListener,
            SimulationCancellationToken cancellationToken
    ) {
        if (progressListener == null || cancellationToken == null) {
            throw new IllegalArgumentException(
                    "Progress listener and cancellation token cannot be null."
            );
        }
        SimulationConfigValidator.validate(config);
        checkCancellation(cancellationToken);
        progressListener.onProgress(0, "Preparing particle definitions");

        SimulationPlan plan = planFactory.createPlan(config);
        List<Particle> particles = plan.getParticles();
        checkCancellation(cancellationToken);
        progressListener.onProgress(
                PARTICLE_DEFINITION_PROGRESS,
                "Particle definitions ready"
        );

        List<ParticleTrack> tracks = new ArrayList<ParticleTrack>(particles.size());
        for (int index = 0; index < particles.size(); index++) {
            checkCancellation(cancellationToken);
            tracks.add(trackGenerator.generate(config, particles.get(index)));
            int progress = PARTICLE_DEFINITION_PROGRESS
                    + (TRACK_GENERATION_PROGRESS_RANGE * (index + 1)
                    / particles.size());
            progressListener.onProgress(
                    progress,
                    "Generated trajectory " + (index + 1) + " of " + particles.size()
            );
        }

        checkCancellation(cancellationToken);
        Scene scene = new Scene(
                config.getImageWidth(),
                config.getImageHeight(),
                config.getMicroscopeConfig().getPixelSizeUm(),
                tracks
        );
        progressListener.onProgress(100, "Trajectory scene complete");
        return new SimulationResult(plan, scene);
    }

    private static void checkCancellation(
            SimulationCancellationToken cancellationToken
    ) {
        if (cancellationToken.isCancellationRequested()) {
            throw new SimulationCancelledException();
        }
    }
}
