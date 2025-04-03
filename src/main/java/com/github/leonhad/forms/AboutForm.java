package com.github.leonhad.forms;

import com.github.leonhad.utils.Constants;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AboutForm extends JDialog {

    public AboutForm(Frame owner) {
        super(owner);
        setResizable(false);

        setTitle("About");

        var panel = new JPanel();
        panel.setLayout(new MigLayout("fill"));

        var image = new JLabel();
        var icon = Toolkit.getDefaultToolkit().getImage(MainForm.class.getResource("/images/icon.png"));
        image.setIcon(new ImageIcon(icon.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        panel.add(image, "align 50% 50%, wrap");

        var title = new JLabel(Constants.TITLE);
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, title.getFont().getSize() * 2));
        panel.add(title, "align 50% 50%, wrap");

        panel.add(new JLabel("Version " + Constants.VERSION), "gapy 10, align 50% 50%, wrap");

        var hyperlink = new JLabel("https://github.com/leonhad/comic-info-editor");
        hyperlink.setForeground(Color.BLUE.darker());
        hyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hyperlink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/leonhad/comic-info-editor"));
                } catch (IOException | URISyntaxException e1) {
                    // Not used.
                }
            }
        });

        panel.add(hyperlink, "align 50% 50%, wrap");

        panel.add(new JLabel("Licence: Apache License Version 2.0 "), "gapy 10, align 50% 50%, wrap");

        var okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());
        panel.add(okButton, "gapy 10, align 50% 50%, wrap");

        okButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        this.setContentPane(panel);
        setVisible(true);
        pack();

        setLocationRelativeTo(owner);
    }
}
