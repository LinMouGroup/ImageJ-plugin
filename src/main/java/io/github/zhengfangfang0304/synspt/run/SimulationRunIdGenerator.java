package io.github.zhengfangfang0304.synspt.run;

import io.github.zhengfangfang0304.synspt.motion.MotionType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

/** Filesystem-safe human-readable identity prefix; the directory factory resolves collisions. */
public final class SimulationRunIdGenerator {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd_HHmmss", Locale.ROOT);

    public String generate(LocalDateTime startedAt, Set<MotionType> models, long rootSeed) {
        if (startedAt == null || models == null || models.isEmpty()
                || startedAt.getYear() < 1 || startedAt.getYear() > 9999) {
            throw new IllegalArgumentException("A valid start time and selected models are required.");
        }
        for (MotionType model : models) {
            if (model == null) { throw new IllegalArgumentException("Model cannot be null."); }
        }
        String model = models.size() == 1 ? models.iterator().next().name() : "MIXED";
        return FORMAT.format(startedAt) + "_" + model + "_seed" + rootSeed;
    }
}
