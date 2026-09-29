package io.github.zhengfangfang0304.synspt.export;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Paths produced by one successful dataset export transaction. */
public final class ExportManifest {

    private final Path tiffPath;
    private final Path trajectoryCsvPath;
    private final Path metadataJsonPath;
    private final List<Path> advancedPaths;

    public ExportManifest(
            Path tiffPath,
            Path trajectoryCsvPath,
            Path metadataJsonPath,
            List<Path> advancedPaths
    ) {
        if (tiffPath == null
                || trajectoryCsvPath == null
                || metadataJsonPath == null
                || advancedPaths == null) {
            throw new IllegalArgumentException("Export paths cannot be null.");
        }
        List<Path> advancedCopy = new ArrayList<Path>(advancedPaths.size());
        for (Path path : advancedPaths) {
            if (path == null) {
                throw new IllegalArgumentException(
                        "Advanced export paths cannot contain null."
                );
            }
            advancedCopy.add(path);
        }
        this.tiffPath = tiffPath;
        this.trajectoryCsvPath = trajectoryCsvPath;
        this.metadataJsonPath = metadataJsonPath;
        this.advancedPaths = Collections.unmodifiableList(advancedCopy);
    }

    public Path getTiffPath() {
        return tiffPath;
    }

    public Path getRunDirectory() {
        return tiffPath.getParent();
    }

    public String getRunId() {
        return getRunDirectory().getFileName().toString();
    }

    public Path getTrajectoryCsvPath() {
        return trajectoryCsvPath;
    }

    public Path getMetadataJsonPath() {
        return metadataJsonPath;
    }

    public List<Path> getAdvancedPaths() {
        return advancedPaths;
    }

    public boolean hasAdvancedExports() {
        return !advancedPaths.isEmpty();
    }
}
