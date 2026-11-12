package com.github.leonhad.fileformat;

import com.github.leonhad.document.ImageMetadata;
import lombok.Getter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Getter
public abstract class FileOpener implements AutoCloseable {

    protected abstract FileFilter[] getFilters();

    public abstract void open(File file) throws IOException;

    public abstract boolean getComicInfo(Predicate<InputStream> consumer);

    public abstract List<ImageMetadata> getImageList();

    public final boolean canOpen(File file) {
        var filters = getFilters();
        return Arrays.stream(filters).anyMatch(f -> f.accept(file));
    }

}
