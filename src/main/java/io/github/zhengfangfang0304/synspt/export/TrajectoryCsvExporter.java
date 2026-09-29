package io.github.zhengfangfang0304.synspt.export;

import io.github.zhengfangfang0304.synspt.engine.RenderedSimulationResult;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancellationToken;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancelledException;
import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes long-form zero-based ground-truth trajectories in UTF-8 CSV. */
public final class TrajectoryCsvExporter {

    static final String HEADER = "particle_id,frame,time_s,x_um,y_um,segment_id,"
            + "motion_type,is_change_point,motion_parameters";

    public void write(
            RenderedSimulationResult result,
            Path outputPath,
            SimulationCancellationToken cancellationToken
    ) throws IOException {
        if (result == null || outputPath == null || cancellationToken == null) {
            throw new IllegalArgumentException(
                    "Result, CSV path, and cancellation token cannot be null."
            );
        }

        BufferedWriter writer = Files.newBufferedWriter(
                outputPath,
                StandardCharsets.UTF_8
        );
        try {
            writer.write(HEADER);
            writer.newLine();
            for (ParticleTrack track : result.getScene().getParticleTracks()) {
                checkCancellation(cancellationToken);
                StateSegment previous = null;
                String parameters = null;
                for (int frame = 0; frame < track.getTrajectory().length(); frame++) {
                    if ((frame & 1023) == 0) {
                        checkCancellation(cancellationToken);
                    }
                    StateSegment segment = track.getParticle().getStateSegmentForFrame(frame);
                    if (segment != previous) {
                        parameters = csvField(MotionParametersFormatter.format(
                                segment.getMotionType(), segment.getMotionParameters()));
                        previous = segment;
                    }
                    writer.write(Integer.toString(track.getParticle().getParticleId()));
                    writer.write(',');
                    writer.write(Integer.toString(frame));
                    writer.write(',');
                    writer.write(Double.toString(frame * result.getPlan().getRequestConfig()
                            .getMicroscopeConfig().getFrameIntervalSeconds()));
                    writer.write(',');
                    writer.write(Double.toString(track.getTrajectory().getXUm(frame)));
                    writer.write(',');
                    writer.write(Double.toString(track.getTrajectory().getYUm(frame)));
                    writer.write(',');
                    writer.write(Integer.toString(segment.getStateIndex()));
                    writer.write(',');
                    writer.write(segment.getMotionType().name());
                    writer.write(',');
                    writer.write(Boolean.toString(segment.getStateIndex() > 0
                            && frame == segment.getStartFrame()));
                    writer.write(',');
                    writer.write(parameters);
                    writer.newLine();
                }
            }
        } finally {
            writer.close();
        }
    }

    static String csvField(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private void checkCancellation(SimulationCancellationToken cancellationToken) {
        if (cancellationToken.isCancellationRequested()) {
            throw new SimulationCancelledException();
        }
    }
}
