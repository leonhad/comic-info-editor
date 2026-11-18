package com.github.leonhad.components;

import com.github.leonhad.integration.ImageCache;
import com.github.leonhad.integration.Manga;
import lombok.extern.java.Log;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

@Log
public class MangaCellRenderer extends JPanel implements ListCellRenderer<Manga> {

    private final JLabel titleLabel;
    private final JLabel description;
    private final JLabel imageLabel;
    private final JPanel contentPanel;

    public MangaCellRenderer() {
        setLayout(new BorderLayout());

        titleLabel = new JLabel();
        description = new JLabel();

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(description, BorderLayout.CENTER);

        imageLabel = new JLabel();

        add(contentPanel, BorderLayout.CENTER);
        add(imageLabel, BorderLayout.WEST);
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Manga> list, Manga value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            titleLabel.setText("");
            description.setText("");
            imageLabel.setIcon(null);
        } else {
            titleLabel.setText(value.getTitle());
            titleLabel.setFont(list.getFont().deriveFont(Font.BOLD));

            String html = value.getDescription();
            if (html != null && !html.isEmpty()) {
                description.setText("<html><div style=\"width:350px\">" + html + "</div></html>");
            } else {
                description.setText("");
            }

            if (value.getCoverImage() != null) {
                try {
                    var image = ImageCache.get(value.getCoverImage());
                    imageLabel.setIcon(image);
                } catch (Exception e) {
                    log.log(Level.WARNING, e, () -> "Failed to load image: " + value.getCoverImage());
                    imageLabel.setIcon(null);
                }
            } else {
                imageLabel.setIcon(null);
            }
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            contentPanel.setBackground(list.getSelectionBackground());
            contentPanel.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            contentPanel.setBackground(list.getBackground());
            contentPanel.setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        return this;
    }
}