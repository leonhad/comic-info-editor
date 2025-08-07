package com.github.leonhad.components;

import com.github.leonhad.integration.ImageCache;
import com.github.leonhad.integration.Manga;

import javax.swing.*;
import java.awt.*;

public class MangaCellRenderer extends JLabel implements ListCellRenderer<Manga> {

    @Override
    public Component getListCellRendererComponent(JList<? extends Manga> list, Manga value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.getTitle());

        if (value.getCoverImage() != null) {
            try {
                var image = ImageCache.get(value.getCoverImage());
                setIcon(image);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }
}