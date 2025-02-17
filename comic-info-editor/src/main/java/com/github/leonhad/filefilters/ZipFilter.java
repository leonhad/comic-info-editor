package com.github.leonhad.filefilters;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class ZipFilter extends FileFilter {

    @Override
    public String getDescription() {
        return "ZIP files (*.zip)";
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        } else {
            String filename = pathname.getName().toLowerCase();
            return filename.endsWith(".zip");
        }
    }

}
