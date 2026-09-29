package io.github.zhengfangfang0304.synspt;

import io.github.zhengfangfang0304.synspt.gui.SyntheticDataGeneratorFrame;

import ij.plugin.PlugIn;

import java.awt.EventQueue;

/**
 * ImageJ 1.x entry point for the synSPT Synthetic Data Generator.
 */
public final class SyntheticDataGeneratorPlugin implements PlugIn {

    @Override
    public void run(String argument) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SyntheticDataGeneratorFrame().setVisible(true);
            }
        });
    }
}
