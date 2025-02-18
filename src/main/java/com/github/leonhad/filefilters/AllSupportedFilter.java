package com.github.leonhad.filefilters;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class AllSupportedFilter extends FileFilter {

    @Override
    public String getDescription() {
        return "All supported files (*.cbz, *.zip)";
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        } else {
            String filename = pathname.getName().toLowerCase();
            return filename.endsWith(".cbz") || filename.endsWith(".zip");
        }
    }

}
