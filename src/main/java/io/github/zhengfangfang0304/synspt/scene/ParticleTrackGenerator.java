package io.github.zhengfangfang0304.synspt.scene;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionContext;
import io.github.zhengfangfang0304.synspt.motion.MotionModel;
import io.github.zhengfangfang0304.synspt.motion.MotionModelFactory;
import io.github.zhengfangfang0304.synspt.motion.MultiStateMotion;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;
import io.github.zhengfangfang0304.synspt.util.SeedDerivation;

import java.util.Random;

/** Converts a model displacement path into an in-field ground-truth track. */
public final class ParticleTrackGenerator {

    private final BoundaryHandler boundaryHandler;

    public ParticleTrackGenerator() {
        this(new ReflectingBoundaryHandler());
    }

    public ParticleTrackGenerator(BoundaryHandler boundaryHandler) {
        if (boundaryHandler == null) {
            throw new IllegalArgumentException(
                    "Boundary handler cannot be null."
            );
        }
        this.boundaryHandler = boundaryHandler;
    }

    public ParticleTrack generate(SimulationConfig config, Particle particle) {
        if (config == null || particle == null) {
            throw new IllegalArgumentException("Config and particle cannot be null.");
        }

        double pixelSizeUm = config.getMicroscopeConfig().getPixelSizeUm();
        double widthUm = config.getImageWidth() * pixelSizeUm;
        double heightUm = config.getImageHeight() * pixelSizeUm;
        Random positionRandom = new Random(SeedDerivation.initialPositionSeed(
                config.getRandomSeed(),
                particle.getParticleId()
        ));
        double initialXUm = positionRandom.nextDouble() * widthUm;
        double initialYUm = positionRandom.nextDouble() * heightUm;

        Trajectory displacement;
        if (particle.getMotionType().isComposite()) {
            displacement = new MultiStateMotion(
                    particle.getStateSegments()
            ).generate(new MotionContext(
                    config.getFrames(),
                    config.getMicroscopeConfig().getFrameIntervalSeconds(),
                    config.getRandomSeed()
            ));
        } else {
            StateSegment state = particle.getStateSegments().get(0);
            MotionModel model = MotionModelFactory.create(
                    state.getMotionParameters()
            );
            displacement = model.generate(new MotionContext(
                    config.getFrames(),
                    config.getMicroscopeConfig().getFrameIntervalSeconds(),
                    SeedDerivation.trajectorySeed(
                            config.getRandomSeed(),
                            particle.getParticleId()
                    )
            ));
        }

        double[] xUm = new double[displacement.length()];
        double[] yUm = new double[displacement.length()];
        for (int frame = 0; frame < displacement.length(); frame++) {
            xUm[frame] = boundaryHandler.apply(
                    initialXUm + displacement.getXUm(frame),
                    widthUm
            );
            yUm[frame] = boundaryHandler.apply(
                    initialYUm + displacement.getYUm(frame),
                    heightUm
            );
        }
        return new ParticleTrack(particle, new Trajectory(xUm, yUm));
    }
}
