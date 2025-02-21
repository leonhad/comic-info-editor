package com.github.leonhad;

import com.github.leonhad.forms.MainForm;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("sun.java2d.uiScale", "1.5");
        System.setProperty("java.awt.headless", "false");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            var frame = new MainForm();
            frame.setVisible(true);
        });
    }
}