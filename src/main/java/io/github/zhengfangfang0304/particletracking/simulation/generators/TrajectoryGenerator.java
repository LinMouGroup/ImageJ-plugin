package io.github.zhengfangfang0304.particletracking.simulation.generators;

import io.github.zhengfangfang0304.particletracking.simulation.config.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticParticle;
import io.github.zhengfangfang0304.particletracking.simulation.motion.DriftModel;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfile;
import io.github.zhengfangfang0304.particletracking.simulation.motion.ParticleMotionAssignment;
import io.github.zhengfangfang0304.particletracking.simulation.physics.PhysicalScaler;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates profile-driven two-dimensional FBM trajectories.
 */
public class TrajectoryGenerator {

    private final PhysicalScaler physicalScaler;

    private final FBMTrajectoryGenerator fbmTrajectoryGenerator;

    public TrajectoryGenerator() {
        this(
                new PhysicalScaler(),
                new FBMTrajectoryGenerator()
        );
    }

    public TrajectoryGenerator(PhysicalScaler physicalScaler) {
        this(
                physicalScaler,
                new FBMTrajectoryGenerator()
        );
    }

    public TrajectoryGenerator(
            PhysicalScaler physicalScaler,
            FBMTrajectoryGenerator fbmTrajectoryGenerator
    ) {
        if (physicalScaler == null) {
            throw new IllegalArgumentException(
                    "Physical scaler cannot be null."
            );
        }
        if (fbmTrajectoryGenerator == null) {
            throw new IllegalArgumentException(
                    "FBM trajectory generator cannot be null."
            );
        }
        this.physicalScaler = physicalScaler;
        this.fbmTrajectoryGenerator = fbmTrajectoryGenerator;
    }

    /**
     * Adds a profile-driven unified FBM trajectory to one particle.
     */
    public void populateTrajectory(
            SyntheticParticle particle,
            SimulationConfig config,
            ParticleMotionAssignment motionAssignment
    ) {
        if (particle == null) {
            throw new IllegalArgumentException(
                    "Synthetic particle cannot be null."
            );
        }
        if (motionAssignment == null) {
            throw new IllegalArgumentException(
                    "Particle motion assignment cannot be null."
            );
        }

        Point2D.Double initialPosition =
                particle.getTrajectory().get(1);
        if (initialPosition == null) {
            throw new IllegalArgumentException(
                    "Particle must have an initial position at frame one."
            );
        }

        List<Point2D.Double> trajectory = generateTrajectory(
                initialPosition,
                config,
                motionAssignment
        );
        for (int sample = 1;
             sample < trajectory.size();
             sample++) {
            Point2D.Double position = trajectory.get(sample);
            particle.addPosition(
                    sample + 1,
                    position.x,
                    position.y
            );
        }
    }

    /**
     * Generates an FBM trajectory from a particle motion assignment.
     */
    public List<Point2D.Double> generateTrajectory(
            Point2D.Double initialPosition,
            SimulationConfig config,
            ParticleMotionAssignment motionAssignment
    ) {
        validateProfileArguments(
                initialPosition,
                config,
                motionAssignment
        );

        int incrementCount = config.frames - 1;
        MotionProfile motionProfile =
                motionAssignment.getMotionProfile();
        Random random = new Random(
                motionAssignment.getTrajectorySeed()
        );
        double[] dimensionlessX = fbmTrajectoryGenerator.generate(
                incrementCount,
                motionProfile,
                random
        );
        double[] dimensionlessY = fbmTrajectoryGenerator.generate(
                incrementCount,
                motionProfile,
                random
        );
        double[] physicalX = physicalScaler.scaleFractionalBrownian(
                dimensionlessX,
                motionProfile,
                config.getImagingConfig()
        );
        double[] physicalY = physicalScaler.scaleFractionalBrownian(
                dimensionlessY,
                motionProfile,
                config.getImagingConfig()
        );
        addPhysicalDrift(
                physicalX,
                physicalY,
                motionProfile.getDriftModel(),
                config
        );
        double[] xDisplacement = physicalScaler.toPixels(
                physicalX,
                config.getImagingConfig()
        );
        double[] yDisplacement = physicalScaler.toPixels(
                physicalY,
                config.getImagingConfig()
        );

        return combineAndReflect(
                initialPosition,
                xDisplacement,
                yDisplacement,
                config
        );
    }

    private void addPhysicalDrift(
            double[] physicalX,
            double[] physicalY,
            DriftModel driftModel,
            SimulationConfig config
    ) {
        double frameInterval = config
                .getImagingConfig()
                .getFrameIntervalSeconds();
        for (int sample = 0;
             sample < physicalX.length;
             sample++) {
            double elapsedTime = sample * frameInterval;
            physicalX[sample] +=
                    driftModel.getVelocityXUmPerSecond()
                            * elapsedTime;
            physicalY[sample] +=
                    driftModel.getVelocityYUmPerSecond()
                            * elapsedTime;
        }
    }

    private List<Point2D.Double> combineAndReflect(
            Point2D.Double initialPosition,
            double[] xDisplacement,
            double[] yDisplacement,
            SimulationConfig config
    ) {
        if (xDisplacement.length != yDisplacement.length) {
            throw new IllegalArgumentException(
                    "X and Y trajectories must have equal lengths."
            );
        }

        List<Point2D.Double> trajectory =
                new ArrayList<>(xDisplacement.length);

        for (int sample = 0;
             sample < xDisplacement.length;
             sample++) {
            trajectory.add(
                    new Point2D.Double(
                            reflectInside(
                                    initialPosition.x
                                            + xDisplacement[sample],
                                    config.width
                            ),
                            reflectInside(
                                    initialPosition.y
                                            + yDisplacement[sample],
                                    config.height
                            )
                    )
            );
        }

        return trajectory;
    }

    private double reflectInside(
            double coordinate,
            int dimension
    ) {
        double maximum = dimension - 1.0;
        if (maximum <= 0.0) {
            return 0.0;
        }

        double period = 2.0 * maximum;
        double reflected = coordinate % period;

        if (reflected < 0.0) {
            reflected += period;
        }
        if (reflected > maximum) {
            reflected = period - reflected;
        }

        return reflected;
    }

    private void validateProfileArguments(
            Point2D.Double initialPosition,
            SimulationConfig config,
            ParticleMotionAssignment motionAssignment
    ) {
        if (initialPosition == null) {
            throw new IllegalArgumentException(
                    "Initial position cannot be null."
            );
        }
        if (config == null) {
            throw new IllegalArgumentException(
                    "Simulation config cannot be null."
            );
        }
        if (motionAssignment == null) {
            throw new IllegalArgumentException(
                    "Particle motion assignment cannot be null."
            );
        }
        config.validate();
    }
}
