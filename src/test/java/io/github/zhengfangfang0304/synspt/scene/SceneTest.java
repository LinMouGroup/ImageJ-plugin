package io.github.zhengfangfang0304.synspt.scene;

import io.github.zhengfangfang0304.synspt.TestFixtures;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SceneTest {

    @Test(expected = UnsupportedOperationException.class)
    public void storesFrameCountAndDefensivelyProtectsTracks() {
        List<ParticleTrack> tracks = new ArrayList<ParticleTrack>();
        tracks.add(track(1, new double[] {0.1, 0.2}, new double[] {0.2, 0.3}));
        Scene scene = new Scene(10, 10, 0.1, tracks);
        tracks.clear();

        assertEquals(1, scene.getParticleTracks().size());
        assertEquals(2, scene.getFrameCount());
        assertEquals(1.0, scene.getWidthUm(), 0.0);
        scene.getParticleTracks().clear();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsCoordinatesOutsideTheHalfOpenField() {
        List<ParticleTrack> tracks = new ArrayList<ParticleTrack>();
        tracks.add(track(1, new double[] {0.1, 1.0}, new double[] {0.2, 0.3}));
        new Scene(10, 10, 0.1, tracks);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsDuplicateParticleIds() {
        List<ParticleTrack> tracks = new ArrayList<ParticleTrack>();
        tracks.add(track(1, new double[] {0.1}, new double[] {0.2}));
        tracks.add(track(1, new double[] {0.3}, new double[] {0.4}));
        new Scene(10, 10, 0.1, tracks);
    }

    private ParticleTrack track(int id, double[] x, double[] y) {
        return new ParticleTrack(
                TestFixtures.particle(id, x.length, 0.1),
                new Trajectory(x, y)
        );
    }
}
