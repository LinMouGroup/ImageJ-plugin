package io.github.zhengfangfang0304.particletracking.simulation.generators;

import io.github.zhengfangfang0304.particletracking.simulation.config.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticParticle;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates synthetic particles and assigns their initial coordinates.
 */
public class ParticleInitializer {

    /**
     * Creates all particles and stores their initial position at frame one.
     */
    public List<SyntheticParticle> initializeParticles(
            SimulationConfig config,
            Random random
    ) {
        validateArguments(config, random);

        List<SyntheticParticle> particles =
                new ArrayList<>(config.particleNumber);

        for (int particleId = 1;
             particleId <= config.particleNumber;
             particleId++) {
            SyntheticParticle particle =
                    new SyntheticParticle(particleId);
            Point2D.Double initialPosition =
                    createInitialPosition(config, random);

            particle.addPosition(
                    1,
                    initialPosition.x,
                    initialPosition.y
            );
            particles.add(particle);
        }

        return particles;
    }

    /**
     * Samples one valid initial coordinate inside the image.
     */
    public Point2D.Double createInitialPosition(
            SimulationConfig config,
            Random random
    ) {
        validateArguments(config, random);

        return new Point2D.Double(
                randomCoordinate(
                        config.width,
                        config.psfSigma,
                        random
                ),
                randomCoordinate(
                        config.height,
                        config.psfSigma,
                        random
                )
        );
    }

    private double randomCoordinate(
            int dimension,
            double psfSigma,
            Random random
    ) {
        double margin =
                coordinateMargin(dimension, psfSigma);
        double upperBound =
                dimension - 1.0 - margin;

        return sampleCoordinate(margin, upperBound, random);
    }

    private double sampleCoordinate(
            double lowerBound,
            double upperBound,
            Random random
    ) {
        if (upperBound <= lowerBound) {
            return lowerBound;
        }

        return lowerBound
                + random.nextDouble()
                * (upperBound - lowerBound);
    }

    private double coordinateMargin(
            int dimension,
            double psfSigma
    ) {
        return Math.min(
                3.0 * psfSigma,
                Math.max(0.0, (dimension - 1.0) / 2.0)
        );
    }

    private void validateArguments(
            SimulationConfig config,
            Random random
    ) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "Simulation config cannot be null."
            );
        }
        if (random == null) {
            throw new IllegalArgumentException(
                    "Random generator cannot be null."
            );
        }
    }
}
