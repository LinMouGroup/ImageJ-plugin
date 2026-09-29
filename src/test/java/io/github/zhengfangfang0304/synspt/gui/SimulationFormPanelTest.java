package io.github.zhengfangfang0304.synspt.gui;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.optical.PsfShapeType;

import org.junit.Test;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SimulationFormPanelTest {

    @Test
    public void defaultsExposePhysicalSamplingAndNineModelChoices() {
        SimulationFormPanel panel = new SimulationFormPanel();
        SimulationConfig config = panel.createConfig(12345L);
        List<JTextField> textFields = components(panel, JTextField.class);
        List<JComboBox> comboBoxes = components(panel, JComboBox.class);
        List<JCheckBox> checkBoxes = components(panel, JCheckBox.class);

        assertEquals(5, textFields.size());
        assertEquals(2, comboBoxes.size());
        assertEquals(9, checkBoxes.size());
        assertEquals("Multi-state Motion", checkBoxes.get(8).getText());
        assertTrue(hasTwoColumnMotionGrid(panel));
        assertEquals(256, config.getImageWidth());
        assertEquals(256, config.getImageHeight());
        assertEquals(100, config.getFrames());
        assertEquals(100, config.getParticleNumber());
        assertEquals(MicroscopeConfig.DEFAULT_PIXEL_SIZE_UM,
                config.getMicroscopeConfig().getPixelSizeUm(), 0.0);
        assertEquals(MicroscopeConfig.DEFAULT_FRAME_INTERVAL_SECONDS,
                config.getMicroscopeConfig().getFrameIntervalSeconds(), 0.0);
        assertEquals(10.0, config.getNoiseConfig().getTargetSnr(), 0.0);
        assertEquals("30", textFields.get(3).getText());
        assertTrue(hasLabel(panel, "Frame Rate (fps):"));
        assertEquals(PsfShapeType.CIRCULAR_GAUSSIAN, config.getSpotShape());
        assertEquals(EnumSet.of(MotionType.BROWNIAN),
                config.getSelectedMotionTypes());
        assertEquals(12345L, config.getRandomSeed());
        assertEquals(ResolutionPreset.DEFAULT, comboBoxes.get(0).getSelectedItem());
        assertEquals(4, comboBoxes.get(0).getItemCount());
        assertEquals(PsfShapeType.CIRCULAR_GAUSSIAN,
                comboBoxes.get(1).getSelectedItem());
        assertEquals("Circular Gaussian",
                comboBoxes.get(1).getItemAt(0).toString());
        assertEquals("Elliptical Gaussian",
                comboBoxes.get(1).getItemAt(1).toString());

        for (int index = 0; index < MotionType.values().length; index++) {
            assertEquals(
                    MotionType.values()[index].getDisplayName(),
                    checkBoxes.get(index).getText()
            );
        }
    }

    @Test
    public void supportsSelectingAllModelsWithoutExposingTheirParameters() {
        SimulationFormPanel panel = new SimulationFormPanel();
        panel.setSelectedMotionTypes(EnumSet.allOf(MotionType.class));
        SimulationConfig config = panel.createConfig(77L);

        assertEquals(EnumSet.allOf(MotionType.class),
                config.getSelectedMotionTypes());
        assertEquals(5, components(panel, JTextField.class).size());
        assertEquals(2, components(panel, JComboBox.class).size());
    }

    @Test
    public void mapsFrameRateAndPixelSizeToPhysicalSimulationConfig() {
        SimulationFormPanel panel = new SimulationFormPanel();
        panel.setPixelSizeText("0.125");
        panel.setFrameRateText("20");
        panel.setSpotShape(PsfShapeType.ELLIPTICAL_GAUSSIAN);

        SimulationConfig config = panel.createConfig(55L);

        assertEquals(0.125,
                config.getMicroscopeConfig().getPixelSizeUm(), 0.0);
        assertEquals(1.0 / 20.0,
                config.getMicroscopeConfig().getFrameIntervalSeconds(), 0.0);
        assertEquals(PsfShapeType.ELLIPTICAL_GAUSSIAN,
                config.getSpotShape());
        assertEquals(MicroscopeConfig.DEFAULT_BACKGROUND_PHOTONS,
                config.getMicroscopeConfig().getBackgroundPhotons(), 0.0);
        assertEquals(MicroscopeConfig.DEFAULT_READ_NOISE_SIGMA_ADU,
                config.getMicroscopeConfig().getReadNoiseSigmaAdu(), 0.0);
        assertEquals(MicroscopeConfig.DEFAULT_GAIN_ADU_PER_PHOTON,
                config.getMicroscopeConfig().getGainAduPerPhoton(), 0.0);
        assertEquals(MicroscopeConfig.DEFAULT_OFFSET_ADU,
                config.getMicroscopeConfig().getOffsetAdu(), 0.0);
        assertEquals(MicroscopeConfig.DEFAULT_BIT_DEPTH,
                config.getMicroscopeConfig().getBitDepth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonNumericFramesBeforeGeneration() {
        SimulationFormPanel panel = new SimulationFormPanel();
        panel.setFramesText("not-a-number");
        panel.createConfig(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveSnrBeforeGeneration() {
        SimulationFormPanel panel = new SimulationFormPanel();
        panel.setSnrText("0");
        panel.createConfig(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositivePixelSizeBeforeGeneration() {
        SimulationFormPanel panel = new SimulationFormPanel();
        panel.setPixelSizeText("0");
        panel.createConfig(1L);
    }

    @Test
    public void convertsCommonFrameRatesToFrameIntervals() {
        double[] frameRates = new double[] {30.0, 60.0, 100.0};
        double[] expectedIntervals = new double[] {
                1.0 / 30.0,
                1.0 / 60.0,
                0.01
        };
        for (int index = 0; index < frameRates.length; index++) {
            SimulationFormPanel panel = new SimulationFormPanel();
            panel.setFrameRateText(Double.toString(frameRates[index]));
            assertEquals(
                    expectedIntervals[index],
                    panel.createConfig(1L).getMicroscopeConfig()
                            .getFrameIntervalSeconds(),
                    0.0
            );
        }
    }

    @Test
    public void rejectsInvalidFrameRatesBeforeGeneration() {
        String[] invalidValues = new String[] {
                "0",
                "-1",
                "NaN",
                "Infinity"
        };
        for (String invalidValue : invalidValues) {
            SimulationFormPanel panel = new SimulationFormPanel();
            panel.setFrameRateText(invalidValue);
            try {
                panel.createConfig(1L);
                fail("Expected invalid frame rate to be rejected: " + invalidValue);
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("Frame Rate"));
            }
        }
    }

    @Test
    public void mapsEveryResolutionPresetToSimulationConfig() {
        SimulationFormPanel panel = new SimulationFormPanel();

        for (ResolutionPreset preset : ResolutionPreset.values()) {
            panel.setResolutionPreset(preset);
            SimulationConfig config = panel.createConfig(99L);
            assertEquals(preset.getWidth(), config.getImageWidth());
            assertEquals(preset.getHeight(), config.getImageHeight());
            assertEquals(
                    preset.getWidth() + " \u00d7 " + preset.getHeight(),
                    preset.toString()
            );
        }
    }

    @Test
    public void disablesEveryEditableControlWhileRunning() {
        SimulationFormPanel panel = new SimulationFormPanel();
        panel.setInputsEnabled(false);

        for (JTextField field : components(panel, JTextField.class)) {
            assertFalse(field.isEnabled());
        }
        for (JComboBox comboBox : components(panel, JComboBox.class)) {
            assertFalse(comboBox.isEnabled());
        }
        for (JCheckBox checkBox : components(panel, JCheckBox.class)) {
            assertFalse(checkBox.isEnabled());
        }
        panel.setInputsEnabled(true);
        for (JComboBox comboBox : components(panel, JComboBox.class)) {
            assertTrue(comboBox.isEnabled());
        }
        for (JCheckBox checkBox : components(panel, JCheckBox.class)) {
            assertTrue(checkBox.isEnabled());
        }
    }

    private <T extends Component> List<T> components(
            Container root,
            Class<T> componentType
    ) {
        List<T> result = new ArrayList<T>();
        collect(root, componentType, result);
        return result;
    }

    private boolean hasLabel(Container root, String expectedText) {
        for (JLabel label : components(root, JLabel.class)) {
            if (expectedText.equals(label.getText())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTwoColumnMotionGrid(Container root) {
        for (JPanel panel : components(root, JPanel.class)) {
            if (panel.getLayout() instanceof GridLayout) {
                GridLayout layout = (GridLayout) panel.getLayout();
                if (layout.getRows() == 0 && layout.getColumns() == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private <T extends Component> void collect(
            Container container,
            Class<T> componentType,
            List<T> result
    ) {
        for (Component component : container.getComponents()) {
            if (componentType.isInstance(component)) {
                result.add(componentType.cast(component));
            }
            if (component instanceof Container) {
                collect((Container) component, componentType, result);
            }
        }
    }
}
