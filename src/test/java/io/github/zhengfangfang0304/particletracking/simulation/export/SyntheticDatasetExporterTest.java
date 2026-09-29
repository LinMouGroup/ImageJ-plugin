package io.github.zhengfangfang0304.particletracking.simulation.export;

import io.github.zhengfangfang0304.particletracking.simulation.config.MotionSelectionConfig;
import io.github.zhengfangfang0304.particletracking.simulation.config.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticDataset;
import io.github.zhengfangfang0304.particletracking.simulation.generators.SyntheticDatasetGenerator;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyntheticDatasetExporterTest {

    @TempDir
    Path outputDirectory;

    @Test
    void exportAddsParticleProfilesWithoutChangingGroundTruth() throws Exception {
        SimulationConfig config = SimulationConfig.defaultConfig();
        config.width = 16;
        config.height = 16;
        config.frames = 3;
        config.particleNumber = 4;
        config.psfSigma = 1.0;
        config.noiseSigma = 0.0;
        MotionSelectionConfig selection = new MotionSelectionConfig(
                EnumSet.of(
                        MotionType.NORMAL_DIFFUSION,
                        MotionType.DIRECTED_ANOMALOUS_DIFFUSION
                )
        );
        config.setMotionSelectionConfig(selection);
        SyntheticDataset dataset =
                new SyntheticDatasetGenerator().generate(
                        config,
                        12345L
                );

        SyntheticDatasetExporter.export(
                dataset,
                outputDirectory.toFile()
        );

        List<String> groundTruth = Files.readAllLines(
                outputDirectory.resolve("ground_truth.csv")
        );
        List<String> profiles = Files.readAllLines(
                outputDirectory.resolve(
                        "particle_motion_profiles.csv"
                )
        );
        assertEquals("frame,particle_id,x,y", groundTruth.get(0));
        assertEquals(
                1 + config.frames * config.particleNumber,
                groundTruth.size()
        );
        assertEquals(
                "particle_id,motion_type,profile_id,H,alpha,K_H,drift,seed",
                profiles.get(0)
        );
        assertEquals(1 + config.particleNumber, profiles.size());
        for (int row = 1; row < profiles.size(); row++) {
            assertEquals(8, profiles.get(row).split(",", -1).length);
        }
        assertTrue(
                profiles.stream().anyMatch(
                        line -> line.contains("CONSTANT_DRIFT")
                )
        );
        assertEquals(selection.getSelectedMotionTypes(), dataset.getMotionTypes());
        assertTrue(Files.exists(outputDirectory.resolve("simulation.tif")));
    }
}
