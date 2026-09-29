package io.github.zhengfangfang0304.synspt.util;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SeedDerivationTest {

    @Test
    public void derivedStreamsAreReproducibleAndDomainSeparated() {
        long rootSeed = 8821L;
        Set<Long> seeds = new HashSet<Long>();
        seeds.add(SeedDerivation.motionCompositionSeed(rootSeed));
        seeds.add(SeedDerivation.motionAssignmentSeed(rootSeed));
        seeds.add(SeedDerivation.psfSizeCompositionSeed(rootSeed));
        seeds.add(SeedDerivation.psfSizeAssignmentSeed(rootSeed));
        for (int particleId = 1; particleId <= 20; particleId++) {
            seeds.add(SeedDerivation.motionParameterSeed(rootSeed, particleId));
            seeds.add(SeedDerivation.trajectorySeed(rootSeed, particleId));
            seeds.add(SeedDerivation.initialPositionSeed(rootSeed, particleId));
            seeds.add(SeedDerivation.psfGeometrySeed(rootSeed, particleId));
            seeds.add(SeedDerivation.brightnessSeed(rootSeed, particleId));
            seeds.add(SeedDerivation.multiStateCountSeed(rootSeed, particleId));
            seeds.add(SeedDerivation.multiStateTypeSeed(rootSeed, particleId));
            seeds.add(SeedDerivation.multiStateChangePointSeed(
                    rootSeed, particleId
            ));
            for (int stateIndex = 0; stateIndex < 5; stateIndex++) {
                seeds.add(SeedDerivation.multiStateParameterSeed(
                        rootSeed, particleId, stateIndex
                ));
                seeds.add(SeedDerivation.multiStateTrajectorySeed(
                        rootSeed, particleId, stateIndex
                ));
            }
        }
        for (int frameIndex = 0; frameIndex < 20; frameIndex++) {
            seeds.add(SeedDerivation.cameraNoiseSeed(rootSeed, frameIndex));
        }

        assertEquals(384, seeds.size());
        assertEquals(
                SeedDerivation.trajectorySeed(rootSeed, 7),
                SeedDerivation.trajectorySeed(rootSeed, 7)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveParticleIds() {
        SeedDerivation.motionParameterSeed(1L, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeCameraFrameIndices() {
        SeedDerivation.cameraNoiseSeed(1L, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeStateIndices() {
        SeedDerivation.multiStateParameterSeed(1L, 1, -1);
    }
}
