package io.github.zhengfangfang0304.synspt.gui;

import org.junit.Test;
import org.junit.Assume;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import static org.junit.Assert.*;

public class ReadmeResourceTest {
    private static final String[] IMAGES = {"Brownian.png", "Directed.png", "Confined.png",
        "FBM.png", "CTRW.png", "LW.png", "SBM.png", "ATTM.png", "multi_state_trajectory.png"};

    private String readHtml() throws Exception {
        URL resource = getClass().getResource("/synspt-readme.html");
        assertNotNull(resource);
        try (java.io.Reader reader = new java.io.InputStreamReader(resource.openStream(), StandardCharsets.UTF_8)) {
            StringBuilder text = new StringBuilder();
            char[] buffer = new char[2048];
            int count;
            while ((count = reader.read(buffer)) != -1) { text.append(buffer, 0, count); }
            return text.toString();
        }
    }

    @Test
    public void guideContainsUsageAndOrderedModelDescriptionImagePairs() throws Exception {
        String html = readHtml();
        assertTrue(html.contains("Quick Start"));
        assertTrue(html.contains("Pixel Size (µm/pixel)"));
        assertTrue(html.contains("simulation.tif"));
        assertTrue(html.contains("trajectory.csv"));
        assertTrue(html.contains("metadata.json"));
        assertFalse(html.contains("Open Full User Guide"));
        assertFalse(html.contains(".pdf"));
        assertFalse(html.contains("<table"));
        java.util.regex.Matcher pairs = java.util.regex.Pattern.compile(
                "<h3>(.*?)</h3>\\s*<p>(.*?)</p>\\s*<img ([^>]+)>",
                java.util.regex.Pattern.DOTALL).matcher(html);
        int i = 0;
        while (pairs.find()) {
            assertTrue(i < IMAGES.length);
            assertTrue(pairs.group(1).startsWith((i + 1) + ". "));
            assertFalse(pairs.group(2).trim().isEmpty());
            assertTrue(pairs.group(3).contains("src=\"readme-images/" + IMAGES[i] + "\""));
            assertTrue(pairs.group(3).contains("width=\"540\""));
            assertFalse(pairs.group(3).contains("height="));
            i++;
        }
        assertEquals(9, i);
    }

    @Test
    public void swingParsesUtf8AndResolvesAllImagesWithoutCharsetInterruption() throws Exception {
        final String html = readHtml();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                JEditorPane pane = new JEditorPane();
                URL base = getClass().getResource("/synspt-readme.html");
                ReadmeDialog.configureHtmlPane(pane, html, base);
                HTMLDocument document = (HTMLDocument) pane.getDocument();
                assertEquals(base, document.getBase());
                assertEquals(Boolean.TRUE, document.getProperty("IgnoreCharsetDirective"));
                try {
                    String text = document.getText(0, document.getLength());
                    assertTrue(text.contains("synSPT Synthetic Data Generator"));
                    assertTrue(text.contains("µm"));
                    assertTrue(text.contains("Lévy Walk"));
                    int count = 0;
                    for (HTMLDocument.Iterator it = document.getIterator(javax.swing.text.html.HTML.Tag.IMG);
                            it.isValid(); it.next()) {
                        String src = it.getAttributes().getAttribute(javax.swing.text.html.HTML.Attribute.SRC).toString();
                        assertEquals("readme-images/" + IMAGES[count++], src);
                        assertNotNull(javax.imageio.ImageIO.read(new URL(document.getBase(), src)));
                    }
                    assertEquals(9, count);
                } catch (Exception e) { throw new AssertionError(e); }
            }
        });
    }

    @Test
    public void dialogKeepsScrollingAndCloseWithoutPdfEntry() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                SyntheticDataGeneratorFrame owner = new SyntheticDataGeneratorFrame();
                try {
                    assertEquals("synSPT Synthetic Data Generator", owner.getTitle());
                    ReadmeDialog dialog = new ReadmeDialog(owner);
                    try {
                        assertEquals("synSPT User Guide", dialog.getTitle());
                        assertTrue(dialog.isResizable());
                        assertNull(findButton(dialog, "Open Full User Guide"));
                        assertNotNull(findButton(dialog, "Close"));
                        dialog.addNotify();
                        dialog.validate();
                        JScrollPane scroll = findScroll(dialog);
                        assertNotNull(scroll);
                        scroll.doLayout();
                        int width = scroll.getViewport().getExtentSize().width;
                        System.out.println("README initial viewport width: " + width);
                        assertTrue("540px image plus body margins must fit", width >= 540 + 36);
                        assertEquals(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, scroll.getVerticalScrollBarPolicy());
                        JEditorPane pane = (JEditorPane) scroll.getViewport().getView();
                        pane.setSize(width, 10000);
                        assertTrue(pane.getPreferredSize().height > scroll.getViewport().getHeight());
                    } finally { dialog.dispose(); }
                } finally { owner.dispose(); }
            }
        });
    }

    private JScrollPane findScroll(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JScrollPane) { return (JScrollPane) component; }
            if (component instanceof Container) {
                JScrollPane found = findScroll((Container) component);
                if (found != null) { return found; }
            }
        }
        return null;
    }

    private JButton findButton(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && text.equals(((JButton) component).getText())) {
                return (JButton) component;
            }
            if (component instanceof Container) {
                JButton found = findButton((Container) component, text);
                if (found != null) { return found; }
            }
        }
        return null;
    }
}
