package com.github.leonhad.components;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageComponent extends JComponent {

    private transient BufferedImage image;

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void paint(Graphics g) {
        if (image != null) {
            float height = (float) this.getHeight() / (float) image.getHeight();
            int width = (int) (image.getWidth() * height);

            g.drawImage(image, (this.getWidth() - width) / 2, 0, width, this.getHeight(), null);
        } else {
            super.paint(g);
        }
    }
}
