package io.github.zhengfangfang0304.synspt.export;

/** Immutable internal selection of default-only or advanced dataset export. */
public final class ExportConfig {

    public static final boolean DEFAULT_EXPORT_ADVANCED = false;

    private final boolean exportAdvanced;

    public ExportConfig(boolean exportAdvanced) {
        this.exportAdvanced = exportAdvanced;
    }

    public static ExportConfig defaultExport() {
        return new ExportConfig(DEFAULT_EXPORT_ADVANCED);
    }

    public static ExportConfig advancedExport() {
        return new ExportConfig(true);
    }

    public boolean isExportAdvanced() {
        return exportAdvanced;
    }
}
