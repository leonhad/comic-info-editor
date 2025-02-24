package com.github.leonhad.filefilters;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class SevenZipFilter extends FileFilter implements java.io.FileFilter {

    @Override
    public String getDescription() {
        return "7-Zip files (*.7z)";
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        } else {
            String filename = pathname.getName().toLowerCase();
            return filename.endsWith(".7z");
        }
    }

}