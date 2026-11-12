package com.github.leonhad.fileformat;

import com.github.leonhad.fileformat.save.EntrySaver;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class FileSaver implements AutoCloseable {

    protected abstract FileFilter[] getFilters();

    public final boolean canWrite(File file) {
        var filters = getFilters();
        return Arrays.stream(filters).anyMatch(f -> f.accept(file));
    }

    public abstract void open(File file) throws IOException;

    public abstract void addEntries(List<EntrySaver> entries) throws IOException;

}
