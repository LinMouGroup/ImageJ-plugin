package io.github.zhengfangfang0304.synspt.run;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

public class SimulationRunDirectoryFactoryTest {
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private final SimulationRunDirectoryFactory factory = new SimulationRunDirectoryFactory(
            Clock.fixed(Instant.parse("2026-09-15T00:45:01Z"), ZoneId.of("Asia/Shanghai")));

    private SimulationConfig config(MotionType... types) {
        return SimulationConfig.builder().randomSeed(12345L)
                .selectedMotionTypes(EnumSet.copyOf(Arrays.asList(types))).build();
    }

    @Test public void namesUseLocalStartTimeAndSelectedModes() throws Exception {
        Path base = temporaryFolder.newFolder().toPath();
        assertEquals("20260915_084501_BROWNIAN_seed12345",
                factory.create(base, config(MotionType.BROWNIAN)).getRunId());
        assertEquals("20260915_084501_MULTI_STATE_seed12345",
                factory.create(base, config(MotionType.MULTI_STATE)).getRunId());
        assertEquals("20260915_084501_MIXED_seed12345",
                factory.create(base, config(MotionType.BROWNIAN, MotionType.DIRECTED, MotionType.FBM)).getRunId());
        assertEquals("20260915_084501_MIXED_seed12345_2",
                factory.create(base, config(MotionType.MULTI_STATE, MotionType.BROWNIAN)).getRunId());
    }

    @Test public void existingDirectoryAndFileArePreservedWithIncreasingSuffix() throws Exception {
        Path base = temporaryFolder.newFolder().toPath();
        SimulationConfig config = config(MotionType.BROWNIAN);
        SimulationRunContext first = factory.create(base, config);
        Path sentinel = first.getDirectory().resolve("keep.txt");
        Files.write(sentinel, "original".getBytes(StandardCharsets.UTF_8));
        Path secondName = base.resolve(first.getRunId() + "_2");
        Files.write(secondName, new byte[] {42});
        SimulationRunContext third = factory.create(base, config);
        assertEquals(first.getRunId() + "_3", third.getRunId());
        assertEquals("original", new String(Files.readAllBytes(sentinel), StandardCharsets.UTF_8));
        assertArrayEquals(new byte[] {42}, Files.readAllBytes(secondName));
        assertSame(config, third.getConfig());
    }

    @Test public void concurrentReservationsAreUnique() throws Exception {
        final Path base = temporaryFolder.newFolder().toPath();
        final SimulationConfig config = config(MotionType.BROWNIAN);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<SimulationRunContext>> futures = new ArrayList<Future<SimulationRunContext>>();
            for (int i = 0; i < 12; i++) {
                futures.add(pool.submit(new Callable<SimulationRunContext>() {
                    @Override public SimulationRunContext call() throws Exception { return factory.create(base, config); }
                }));
            }
            Set<String> ids = new HashSet<String>();
            for (Future<SimulationRunContext> future : futures) {
                SimulationRunContext run = future.get(10, TimeUnit.SECONDS);
                assertTrue(Files.isDirectory(run.getDirectory()));
                assertTrue(ids.add(run.getRunId()));
            }
            assertEquals(12, ids.size());
        } finally { pool.shutdownNow(); }
    }

    @Test public void negativeSeedRemainsExactAndWindowsSafe() throws Exception {
        SimulationConfig config = SimulationConfig.builder().randomSeed(Long.MIN_VALUE)
                .selectedMotionTypes(EnumSet.of(MotionType.FBM)).build();
        String id = factory.create(temporaryFolder.newFolder().toPath(), config).getRunId();
        assertEquals("20260915_084501_FBM_seed-9223372036854775808", id);
        assertTrue(id.matches("[A-Z0-9_a-z-]+"));
    }
}
