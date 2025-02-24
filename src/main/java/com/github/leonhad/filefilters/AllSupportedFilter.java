package com.github.leonhad.filefilters;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class AllSupportedFilter extends FileFilter {

    @Override
    public String getDescription() {
        return "All supported files (*cb7, *.cbr, *.cbt, *.cbz, 7z, *.rar, *.tar, *.zip)";
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        } else {
            String filename = pathname.getName().toLowerCase();
            return filename.endsWith(".cb7") || filename.endsWith(".7z")
                    || filename.endsWith(".cbz") || filename.endsWith(".zip")
                    || filename.endsWith(".cbr") || filename.endsWith(".rar")
                    || filename.endsWith(".cbt") || filename.endsWith(".tar");
        }
    }

}
