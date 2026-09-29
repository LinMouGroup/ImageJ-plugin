package io.github.zhengfangfang0304.synspt.export;

import io.github.zhengfangfang0304.synspt.engine.RenderedSimulationResult;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancellationToken;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancelledException;
import io.github.zhengfangfang0304.synspt.optical.ParticleImagingParameters;
import io.github.zhengfangfang0304.synspt.optical.ParticlePsfParameters;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Writes lifetime-stable imaging and realized motion properties per particle. */
public final class ParticlePropertiesCsvExporter {

    private static final String HEADER = "particle_id,brightness_photons_per_frame,"
            + "psf_shape,psf_size_class,radius_pixels,sigma_x_pixels,sigma_y_pixels,"
            + "rotation_radians,motion_parameters";

    public void write(
            RenderedSimulationResult result,
            Path outputPath,
            SimulationCancellationToken cancellationToken
    ) throws IOException {
        if (result == null || outputPath == null || cancellationToken == null) {
            throw new IllegalArgumentException(
                    "Result, properties path, and cancellation token cannot be null."
            );
        }
        BufferedWriter writer = Files.newBufferedWriter(
                outputPath,
                StandardCharsets.UTF_8
        );
        try {
            writer.write(HEADER);
            writer.newLine();
            for (Particle particle : result.getPlan().getParticles()) {
                checkCancellation(cancellationToken);
                ParticleImagingParameters imaging = particle.getImagingParameters();
                ParticlePsfParameters psf = imaging.getPsfParameters();
                writer.write(Integer.toString(particle.getParticleId()));
                writer.write(',');
                writer.write(Double.toString(imaging.getEmitterPhotonsPerFrame()));
                writer.write(',');
                writer.write(psf.getShapeType().name());
                writer.write(',');
                writer.write(psf.getSizeClass().name());
                writer.write(',');
                writer.write(Double.toString(psf.getRadiusPixels()));
                writer.write(',');
                writer.write(Double.toString(psf.getSigmaXPixels()));
                writer.write(',');
                writer.write(Double.toString(psf.getSigmaYPixels()));
                writer.write(',');
                writer.write(Double.toString(psf.getRotationRadians()));
                writer.write(',');
                writer.write(csvField(motionParameterText(particle)));
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private String motionParameterText(Particle particle) {
        StringBuilder builder = new StringBuilder();
        for (StateSegment segment : particle.getStateSegments()) {
            if (builder.length() > 0) {
                builder.append('|');
            }
            builder.append(segment.getMotionType().name()).append('{');
            int parameterIndex = 0;
            for (Map.Entry<String, String> entry
                    : segment.getMotionParameters().toMetadata().entrySet()) {
                if (parameterIndex > 0) {
                    builder.append(';');
                }
                builder.append(entry.getKey())
                        .append('=')
                        .append(entry.getValue());
                parameterIndex++;
            }
            builder.append('}');
        }
        return builder.toString();
    }

    private String csvField(String value) {
        if (value.indexOf(',') < 0
                && value.indexOf('"') < 0
                && value.indexOf('\n') < 0
                && value.indexOf('\r') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private void checkCancellation(SimulationCancellationToken cancellationToken) {
        if (cancellationToken.isCancellationRequested()) {
            throw new SimulationCancelledException();
        }
    }
}
