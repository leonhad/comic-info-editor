package com.github.leonhad.components;

import java.awt.image.RGBImageFilter;

public class SelectedImageFilter extends RGBImageFilter {

    private final float rf;
    private final float gf;
    private final float bf;

    protected SelectedImageFilter(float rf, float gf, float bf) {
        super();
        this.rf = Math.min(1f, rf);
        this.gf = Math.min(1f, gf);
        this.bf = Math.min(1f, bf);
        canFilterIndexColorModel = false;
    }

    @Override
    public int filterRGB(int x, int y, int argb) {
        int r = Math.round(((argb >> 16) & 0xFF) * rf);
        int g = Math.round(((argb >> 8) & 0xFF) * gf);
        int b = Math.round((argb & 0xFF) * bf);
        return (argb & 0xFF_00_00_00) | (r << 16) | (g << 8) | b;
    }
}
