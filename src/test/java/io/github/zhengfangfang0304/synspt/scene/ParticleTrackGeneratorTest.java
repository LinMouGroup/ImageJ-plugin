package io.github.zhengfangfang0304.synspt.scene;

import io.github.zhengfangfang0304.synspt.TestFixtures;
import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ParticleTrackGeneratorTest {

    @Test
    public void translatesAndReflectsEveryPointInsideThePhysicalField() {
        SimulationConfig config = SimulationConfig.builder()
                .imageWidth(5)
                .imageHeight(4)
                .frames(500)
                .particleNumber(1)
                .randomSeed(8181L)
                .build();
        Particle particle = TestFixtures.particle(1, config.getFrames(), 5.0);
        ParticleTrack track = new ParticleTrackGenerator().generate(config, particle);
        double widthUm = config.getImageWidth()
                * config.getMicroscopeConfig().getPixelSizeUm();
        double heightUm = config.getImageHeight()
                * config.getMicroscopeConfig().getPixelSizeUm();

        for (int frame = 0; frame < track.getTrajectory().length(); frame++) {
            double x = track.getTrajectory().getXUm(frame);
            double y = track.getTrajectory().getYUm(frame);
            assertTrue(x >= 0.0 && x < widthUm);
            assertTrue(y >= 0.0 && y < heightUm);
        }
    }

    @Test
    public void sameConfigurationAndParticleProduceTheSameAbsoluteTrack() {
        SimulationConfig config = SimulationConfig.builder()
                .imageWidth(32)
                .imageHeight(24)
                .frames(100)
                .particleNumber(1)
                .randomSeed(7272L)
                .build();
        Particle particle = TestFixtures.particle(1, config.getFrames(), 0.1);
        ParticleTrackGenerator generator = new ParticleTrackGenerator();
        ParticleTrack first = generator.generate(config, particle);
        ParticleTrack second = generator.generate(config, particle);

        assertArrayEquals(
                first.getTrajectory().copyXUm(),
                second.getTrajectory().copyXUm(),
                0.0
        );
        assertArrayEquals(
                first.getTrajectory().copyYUm(),
                second.getTrajectory().copyYUm(),
                0.0
        );
    }

}
