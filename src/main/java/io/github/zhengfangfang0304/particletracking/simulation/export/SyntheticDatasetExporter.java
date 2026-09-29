package io.github.zhengfangfang0304.particletracking.simulation.export;

import ij.ImagePlus;
import ij.io.FileSaver;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticDataset;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticParticle;
import io.github.zhengfangfang0304.particletracking.simulation.motion.DriftModel;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfile;
import io.github.zhengfangfang0304.particletracking.simulation.motion.ParticleMotionAssignment;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Saves a synthetic image stack and its ground-truth trajectories.
 */
public final class SyntheticDatasetExporter {

    private SyntheticDatasetExporter() {
    }

    public static void export(
            SyntheticDataset dataset,
            File outputDirectory
    ) throws IOException {
        if (dataset == null) {
            throw new IllegalArgumentException(
                    "Synthetic dataset cannot be null."
            );
        }
        if (outputDirectory == null) {
            throw new IllegalArgumentException(
                    "Output directory cannot be null."
            );
        }

        createOutputDirectory(outputDirectory);
        saveImage(
                dataset.getImage(),
                new File(outputDirectory, "simulation.tif")
        );
        saveGroundTruth(
                dataset,
                new File(outputDirectory, "ground_truth.csv")
        );
        saveParticleMotionProfiles(
                dataset,
                new File(
                        outputDirectory,
                        "particle_motion_profiles.csv"
                )
        );
    }

    private static void createOutputDirectory(
            File outputDirectory
    ) throws IOException {
        if (outputDirectory.exists()) {
            if (!outputDirectory.isDirectory()) {
                throw new IOException(
                        "Output path is not a directory: "
                                + outputDirectory.getAbsolutePath()
                );
            }
            return;
        }

        if (!outputDirectory.mkdirs()) {
            throw new IOException(
                    "Cannot create output directory: "
                            + outputDirectory.getAbsolutePath()
            );
        }
    }

    private static void saveImage(
            ImagePlus image,
            File outputFile
    ) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException(
                    "Synthetic image cannot be null."
            );
        }

        FileSaver saver = new FileSaver(image);
        boolean saved;

        if (image.getStackSize() > 1) {
            saved = saver.saveAsTiffStack(outputFile.getAbsolutePath());
        } else {
            saved = saver.saveAsTiff(outputFile.getAbsolutePath());
        }

        if (!saved) {
            throw new IOException(
                    "Cannot save TIFF image: "
                            + outputFile.getAbsolutePath()
            );
        }
    }

    private static void saveGroundTruth(
            SyntheticDataset dataset,
            File outputFile
    ) throws IOException {
        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(outputFile)
                        )
        ) {
            writer.println("frame,particle_id,x,y");

            for (SyntheticParticle particle : dataset.getParticles()) {
                for (
                        Map.Entry<Integer, Point2D.Double> entry
                        : particle.getTrajectory().entrySet()
                ) {
                    Point2D.Double position = entry.getValue();
                    writer.println(
                            entry.getKey()
                                    + ","
                                    + particle.getParticleId()
                                    + ","
                                    + position.x
                                    + ","
                                    + position.y
                    );
                }
            }
        }
    }

    private static void saveParticleMotionProfiles(
            SyntheticDataset dataset,
            File outputFile
    ) throws IOException {
        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(outputFile)
                        )
        ) {
            writer.println(
                    "particle_id,motion_type,profile_id,H,alpha,K_H,drift,seed"
            );

            for (SyntheticParticle particle : dataset.getParticles()) {
                ParticleMotionAssignment assignment =
                        particle.getMotionAssignment();
                if (assignment == null) {
                    continue;
                }

                MotionProfile profile = assignment.getMotionProfile();
                writer.println(
                        particle.getParticleId()
                                + ","
                                + profile.getMotionType().name()
                                + ","
                                + profile.getProfileId()
                                + ","
                                + profile.getHurstExponent()
                                + ","
                                + profile.getAlpha()
                                + ","
                                + profile
                                .getGeneralizedDiffusionCoefficient()
                                + ","
                                + driftMetadata(profile.getDriftModel())
                                + ","
                                + assignment.getTrajectorySeed()
                );
            }
        }
    }

    private static String driftMetadata(DriftModel driftModel) {
        if (driftModel.isNone()) {
            return DriftModel.Type.NONE.name();
        }
        return DriftModel.Type.CONSTANT_DRIFT.name()
                + "(vx="
                + driftModel.getVelocityXUmPerSecond()
                + "um/s;vy="
                + driftModel.getVelocityYUmPerSecond()
                + "um/s)";
    }
}
