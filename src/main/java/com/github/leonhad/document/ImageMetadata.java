package com.github.leonhad.document;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;

@Log
@Getter
public class ImageMetadata {

    private final String fileName;

    @Setter
    private byte[] image;

    private BufferedImage buffer;

    public ImageMetadata(String fileName) {
        this.fileName = fileName;
    }

    public ImageMetadata(String fileName, byte[] image) {
        this.fileName = fileName;
        this.image = image;
    }

    public int getWidth() {
        return Optional.ofNullable( getBuffer()).map(BufferedImage::getWidth).orElse(0);
    }

    public int getHeight() {
        return Optional.ofNullable( getBuffer()).map(BufferedImage::getHeight).orElse(0);
    }

    public BufferedImage getBuffer() {
        if (this.buffer == null) {
            try (var bais = new ByteArrayInputStream(image)) {
                this.buffer = ImageIO.read(bais);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Unexpected error loading image.", e);
            }
        }

        return buffer;
    }
}
