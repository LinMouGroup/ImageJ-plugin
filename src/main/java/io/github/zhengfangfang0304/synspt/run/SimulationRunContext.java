package io.github.zhengfangfang0304.synspt.run;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import java.nio.file.Path;
import java.time.ZonedDateTime;

/** A reserved run directory and the exact immutable configuration used to generate it. */
public final class SimulationRunContext {
    private final SimulationConfig config;
    private final Path directory;
    private final ZonedDateTime startedAt;
    private boolean exportStarted;

    SimulationRunContext(SimulationConfig config, Path directory, ZonedDateTime startedAt) {
        this.config = config;
        this.directory = directory;
        this.startedAt = startedAt;
    }

    public String getRunId() { return directory.getFileName().toString(); }
    public Path getDirectory() { return directory; }
    public SimulationConfig getConfig() { return config; }
    public ZonedDateTime getStartedAt() { return startedAt; }

    public void requireConfig(SimulationConfig actual) {
        if (actual != config) {
            throw new IllegalArgumentException("Run must use the exact configuration reserved before generation.");
        }
    }

    /** A context cannot be reused, including after cancellation or an export failure. */
    public synchronized void beginExport(SimulationConfig actual) {
        requireConfig(actual);
        if (exportStarted) {
            throw new IllegalStateException("This run already had an export attempt; create a new run.");
        }
        exportStarted = true;
    }
}
