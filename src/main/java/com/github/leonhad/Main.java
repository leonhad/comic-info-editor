package com.github.leonhad;

import com.github.leonhad.forms.MainForm;
import com.github.leonhad.forms.SearchForm;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("sun.java2d.uiScale", "1.5");
        System.setProperty("java.awt.headless", "false");

        try {
            SevenZip.initSevenZipFromPlatformJAR();
            System.out.println("7-Zip-JBinding library was initialized");
        } catch (SevenZipNativeInitializationException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            var frame = new MainForm(args);
            frame.setVisible(true);
        });
    }
}