package io.github.zhengfangfang0304.synspt.run;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.config.SimulationConfigValidator;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;

/** Reserves a fresh directory with an atomic create, including across concurrent processes. */
public final class SimulationRunDirectoryFactory {
    private final Clock clock;
    private final SimulationRunIdGenerator idGenerator = new SimulationRunIdGenerator();

    public SimulationRunDirectoryFactory() { this(Clock.systemDefaultZone()); }

    public SimulationRunDirectoryFactory(Clock clock) {
        if (clock == null) { throw new IllegalArgumentException("Clock cannot be null."); }
        this.clock = clock;
    }

    public SimulationRunContext create(Path baseDirectory, SimulationConfig config) throws IOException {
        SimulationConfigValidator.validate(config);
        if (baseDirectory == null) { throw new IllegalArgumentException("Output directory is required."); }
        Path base = baseDirectory.toAbsolutePath().normalize();
        if (!Files.isDirectory(base) || !Files.isWritable(base)) {
            throw new IllegalArgumentException("Output location must be an existing writable directory.");
        }
        ZonedDateTime startedAt = ZonedDateTime.now(clock);
        String prefix = idGenerator.generate(startedAt.toLocalDateTime(),
                config.getSelectedMotionTypes(), config.getRandomSeed());
        for (long suffix = 1; suffix > 0; suffix++) {
            Path candidate = base.resolve(prefix + (suffix == 1 ? "" : "_" + suffix));
            try {
                Files.createDirectory(candidate);
                return new SimulationRunContext(config, candidate, startedAt);
            } catch (FileAlreadyExistsException collision) {
                // Existing directories, files and links are never touched.
            }
        }
        throw new IOException("Run directory suffix space exhausted.");
    }
}
