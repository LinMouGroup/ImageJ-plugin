package io.github.zhengfangfang0304.synspt.export;

import io.github.zhengfangfang0304.synspt.engine.RenderedSimulationResult;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancellationToken;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancelledException;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes inclusive changepoint intervals for every trajectory segment. */
public final class TrajectorySegmentsCsvExporter {

    private static final String HEADER =
            "particle_id,start_frame,end_frame,motion_state";

    public void write(
            RenderedSimulationResult result,
            Path outputPath,
            SimulationCancellationToken cancellationToken
    ) throws IOException {
        if (result == null || outputPath == null || cancellationToken == null) {
            throw new IllegalArgumentException(
                    "Result, segment path, and cancellation token cannot be null."
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
                for (StateSegment state : particle.getStateSegments()) {
                    writer.write(Integer.toString(particle.getParticleId()));
                    writer.write(',');
                    writer.write(Integer.toString(state.getStartFrame()));
                    writer.write(',');
                    writer.write(Integer.toString(state.getEndFrame()));
                    writer.write(',');
                    writer.write(state.getMotionType().name());
                    writer.newLine();
                }
            }
        } finally {
            writer.close();
        }
    }

    private void checkCancellation(SimulationCancellationToken token) {
        if (token.isCancellationRequested()) {
            throw new SimulationCancelledException();
        }
    }
}
