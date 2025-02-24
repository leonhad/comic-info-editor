package com.github.leonhad.filefilters;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class TarFilter extends FileFilter implements java.io.FileFilter {

    @Override
    public String getDescription() {
        return "Tar files (*.tar)";
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        } else {
            String filename = pathname.getName().toLowerCase();
            return filename.endsWith(".tar");
        }
    }

}