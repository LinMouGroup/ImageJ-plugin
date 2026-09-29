package io.github.zhengfangfang0304.synspt.gui;

import ij.IJ;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;

/** Offline, modeless user guide owned by the synthetic-data window. */
public final class ReadmeDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final String README_RESOURCE = "/synspt-readme.html";
    private static final int INITIAL_WIDTH = 650;
    private static final int INITIAL_HEIGHT = 620;
    private static final int MINIMUM_WIDTH = 500;
    private static final int MINIMUM_HEIGHT = 500;

    private final JEditorPane editorPane;
    private final URL readmeResourceUrl;

    public ReadmeDialog(SyntheticDataGeneratorFrame owner) {
        super(owner, "synSPT User Guide", ModalityType.MODELESS);
        if (owner == null) {
            throw new IllegalArgumentException("README owner cannot be null.");
        }

        readmeResourceUrl = ReadmeDialog.class.getResource(README_RESOURCE);
        editorPane = createEditorPane();
        buildWindow(owner);
    }

    /** Shows the reusable dialog and returns its document to the top. */
    void showAtTop() {
        editorPane.setCaretPosition(0);
        setLocationRelativeTo(getOwner());
        setVisible(true);
        toFront();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                editorPane.setCaretPosition(0);
            }
        });
    }

    private JEditorPane createEditorPane() {
        JEditorPane pane = new JEditorPane();
        configureHtmlPane(pane, loadReadmeHtml(), readmeResourceUrl);
        pane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event) {
                followInternalAnchor(event);
            }
        });
        return pane;
    }

    static void configureHtmlPane(
            JEditorPane pane,
            String html,
            URL baseUrl
    ) {
        if (pane == null || html == null) {
            throw new IllegalArgumentException(
                    "HTML pane and README content cannot be null."
            );
        }
        pane.setContentType("text/html");
        pane.setEditable(false);
        Document document = pane.getDocument();
        if (document instanceof HTMLDocument) {
            ((HTMLDocument) document).putProperty(
                    "IgnoreCharsetDirective",
                    Boolean.TRUE
            );
            // Resolve images before parsing creates their Swing views, including jar: URLs.
            if (baseUrl != null) {
                ((HTMLDocument) document).setBase(baseUrl);
            }
        }
        pane.setText(html);
        pane.setCaretPosition(0);
    }

    private void buildWindow(SyntheticDataGeneratorFrame owner) {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setVisible(false);
            }
        });
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        controls.add(closeButton);
        add(controls, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    private void followInternalAnchor(HyperlinkEvent event) {
        if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
            return;
        }

        String description = event.getDescription();
        if (description != null && description.startsWith("#")) {
            editorPane.scrollToReference(description.substring(1));
            return;
        }

        URL link = event.getURL();
        if (readmeResourceUrl != null
                && link != null
                && readmeResourceUrl.sameFile(link)
                && link.getRef() != null) {
            editorPane.scrollToReference(link.getRef());
        }
    }

    private String loadReadmeHtml() {
        if (readmeResourceUrl == null) {
            String message = "README resource was not found: " + README_RESOURCE;
            IJ.log("synSPT " + message);
            return errorHtml(message);
        }

        try {
            return readUtf8(readmeResourceUrl);
        } catch (IOException exception) {
            IJ.log("synSPT README could not be loaded: " + exception.toString());
            return errorHtml("The offline README could not be loaded.");
        }
    }

    private String readUtf8(URL resourceUrl) throws IOException {
        InputStream stream = resourceUrl.openStream();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
        try {
            StringBuilder html = new StringBuilder(32768);
            char[] buffer = new char[4096];
            int count;
            while ((count = reader.read(buffer)) >= 0) {
                html.append(buffer, 0, count);
            }
            return html.toString();
        } finally {
            reader.close();
        }
    }

    private String errorHtml(String message) {
        return "<html><body><h1>synSPT User Guide</h1><p>"
                + escapeHtml(message)
                + "</p></body></html>";
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
