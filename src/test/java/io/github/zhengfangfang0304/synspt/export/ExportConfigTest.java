package io.github.zhengfangfang0304.synspt.export;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExportConfigTest {

    @Test
    public void defaultExportDisablesAdvancedFiles() {
        assertFalse(ExportConfig.DEFAULT_EXPORT_ADVANCED);
        assertFalse(ExportConfig.defaultExport().isExportAdvanced());
    }

    @Test
    public void advancedExportEnablesAdvancedFiles() {
        assertTrue(ExportConfig.advancedExport().isExportAdvanced());
        assertTrue(new ExportConfig(true).isExportAdvanced());
    }
}
