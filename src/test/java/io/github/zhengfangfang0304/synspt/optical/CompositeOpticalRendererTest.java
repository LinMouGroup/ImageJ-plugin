package io.github.zhengfangfang0304.synspt.optical;

import io.github.zhengfangfang0304.synspt.TestFixtures;
import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;
import io.github.zhengfangfang0304.synspt.scene.Scene;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class CompositeOpticalRendererTest {

    @Test
    public void convertsMicrometreCoordinatesUsingTheScenePixelSize() {
        RecordingRenderer circular = new RecordingRenderer(
                PsfShapeType.CIRCULAR_GAUSSIAN
        );
        RecordingRenderer elliptical = new RecordingRenderer(
                PsfShapeType.ELLIPTICAL_GAUSSIAN
        );
        CompositeOpticalRenderer renderer = new CompositeOpticalRenderer(
                new PsfKernelRenderer[] {circular, elliptical}
        );

        renderer.renderFrame(
                scene(0.1),
                0,
                microscopeConfig(0.1)
        );
        assertEquals(10.0, circular.centerX, 0.0);
        assertEquals(10.0, circular.centerY, 0.0);

        renderer.renderFrame(
                scene(0.2),
                0,
                microscopeConfig(0.2)
        );
        assertEquals(5.0, circular.centerX, 0.0);
        assertEquals(5.0, circular.centerY, 0.0);
        assertEquals(2, circular.renderCount);
        assertEquals(0, elliptical.renderCount);
    }

    private Scene scene(double pixelSizeUm) {
        Particle particle = TestFixtures.particle(1, 1, 0.1);
        ParticleTrack track = new ParticleTrack(
                particle,
                new Trajectory(
                        new double[] {1.0},
                        new double[] {1.0}
                )
        );
        return new Scene(
                20,
                20,
                pixelSizeUm,
                Collections.singletonList(track)
        );
    }

    private MicroscopeConfig microscopeConfig(double pixelSizeUm) {
        return new MicroscopeConfig(
                pixelSizeUm,
                MicroscopeConfig.DEFAULT_FRAME_INTERVAL_SECONDS,
                MicroscopeConfig.DEFAULT_BACKGROUND_PHOTONS,
                MicroscopeConfig.DEFAULT_READ_NOISE_SIGMA_ADU,
                MicroscopeConfig.DEFAULT_GAIN_ADU_PER_PHOTON,
                MicroscopeConfig.DEFAULT_OFFSET_ADU,
                MicroscopeConfig.DEFAULT_BIT_DEPTH
        );
    }

    private static final class RecordingRenderer implements PsfKernelRenderer {

        private final PsfShapeType shapeType;
        private double centerX;
        private double centerY;
        private int renderCount;

        private RecordingRenderer(PsfShapeType shapeType) {
            this.shapeType = shapeType;
        }

        @Override
        public PsfShapeType getShapeType() {
            return shapeType;
        }

        @Override
        public void render(
                double[] expectedPhotons,
                int width,
                int height,
                double centerX,
                double centerY,
                ParticleImagingParameters imagingParameters
        ) {
            this.centerX = centerX;
            this.centerY = centerY;
            renderCount++;
        }
    }
}
