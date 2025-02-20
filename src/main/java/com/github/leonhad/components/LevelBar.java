package com.github.leonhad.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LevelBar extends JPanel {

    private final List<ImageIcon> iconList;

    private final List<ImageIcon> iconRoverList;

    private final List<JLabel> labelList = Arrays.asList(
            new JLabel(), new JLabel(), new JLabel(), new JLabel(), new JLabel()
    );

    protected ImageIcon defaultIcon = null;

    protected int clicked = -1;

    private transient MouseAdapter handler;

    public LevelBar() {
        super(new GridLayout(1, 5, 4, 4));

        var imageHi = getImage();
        this.defaultIcon = new ImageIcon(imageHi);

        var star = makeStarImageIcon(imageHi, new SelectedImageFilter(1f, 1f, 0f));
        this.iconList = Arrays.asList(star, star, star, star, star);

        var star2 = makeStarImageIcon(imageHi, new SelectedImageFilter(.8f, .8f, 0f));
        this.iconRoverList = Arrays.asList(star2, star2, star2, star2, star2);

        EventQueue.invokeLater(() -> {
            for (JLabel l : labelList) {
                l.setIcon(defaultIcon);
                add(l);
            }
        });
    }

    private Image getImage() {
        try (var s = LevelBar.class.getResourceAsStream("/images/star.png")) {
            if (s != null) {
                return ImageIO.read(s);
            }

        } catch (IOException e) {
            // Never throws.
        }

        return new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    }

    private static ImageIcon makeStarImageIcon(Image imageHi, ImageFilter filter) {
        var producerHi = new FilteredImageSource(imageHi.getSource(), filter);
        var hi = Toolkit.getDefaultToolkit().createImage(producerHi);
        return new ImageIcon(hi);
    }

    @Override
    public void updateUI() {
        removeMouseListener(handler);
        removeMouseMotionListener(handler);
        super.updateUI();
        handler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                repaintRoverIcon(getSelectedIconIndex(e.getPoint()));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                repaintRoverIcon(getSelectedIconIndex(e.getPoint()));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() > MouseEvent.BUTTON1) {
                    clicked = -1;
                    repaintRoverIcon(getSelectedIconIndex(e.getPoint()));
                } else {
                    clicked = getSelectedIconIndex(e.getPoint());
                    repaintIcon(clicked);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                repaintIcon(clicked);
            }
        };

        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    protected int getSelectedIconIndex(Point p) {
        for (int i = 0; i < labelList.size(); i++) {
            Rectangle r = labelList.get(i).getBounds();
            r.grow(2, 2);
            if (r.contains(p)) {
                return i;
            }
        }
        return -1;
    }

    protected void repaintIcon(int index) {
        for (int i = 0; i < labelList.size(); i++) {
            labelList.get(i).setIcon(i <= index ? iconList.get(i) : defaultIcon);
        }

        repaint();
    }

    protected void repaintRoverIcon(int index) {
        if (index == clicked) {
            repaintIcon(index);
        } else {
            for (int i = 0; i < labelList.size(); i++) {
                labelList.get(i).setIcon(i <= index ? iconRoverList.get(i) : (i <= clicked ? iconList.get(i) : defaultIcon));
            }

            repaint();
        }
    }
}