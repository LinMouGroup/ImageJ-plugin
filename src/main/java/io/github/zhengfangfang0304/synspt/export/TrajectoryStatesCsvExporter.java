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
import java.util.Map;

/** Writes one row for every realized basic-motion state segment. */
public final class TrajectoryStatesCsvExporter {

    private static final String HEADER =
            "particle_id,state_id,motion_type,start_frame,end_frame,parameters";

    public void write(
            RenderedSimulationResult result,
            Path outputPath,
            SimulationCancellationToken cancellationToken
    ) throws IOException {
        if (result == null || outputPath == null || cancellationToken == null) {
            throw new IllegalArgumentException(
                    "Result, state path, and cancellation token cannot be null."
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
                    writer.write(Integer.toString(state.getStateIndex()));
                    writer.write(',');
                    writer.write(state.getMotionType().name());
                    writer.write(',');
                    writer.write(Integer.toString(state.getStartFrame()));
                    writer.write(',');
                    writer.write(Integer.toString(state.getEndFrame()));
                    writer.write(',');
                    writer.write(csvField(parameterText(state)));
                    writer.newLine();
                }
            }
        } finally {
            writer.close();
        }
    }

    private String parameterText(StateSegment state) {
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, String> entry
                : state.getMotionParameters().toMetadata().entrySet()) {
            if (text.length() > 0) {
                text.append(';');
            }
            text.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return text.toString();
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

    private void checkCancellation(SimulationCancellationToken token) {
        if (token.isCancellationRequested()) {
            throw new SimulationCancelledException();
        }
    }
}
