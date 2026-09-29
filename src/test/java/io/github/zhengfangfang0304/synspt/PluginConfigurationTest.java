package io.github.zhengfangfang0304.synspt;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PluginConfigurationTest {

    @Test
    public void registersTheRequiredNestedFijiMenu() throws IOException {
        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("plugins.config");
        assertNotNull(stream);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
        try {
            assertEquals(
                "Plugins>synSPT, \"Synthetic Data Generator\", "
                        + "io.github.zhengfangfang0304.synspt.SyntheticDataGeneratorPlugin",
                    reader.readLine()
            );
        } finally {
            reader.close();
        }
    }
}
