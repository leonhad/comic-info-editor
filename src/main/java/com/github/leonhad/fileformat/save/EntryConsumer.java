package com.github.leonhad.fileformat.save;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface EntryConsumer {

    void accept(OutputStream stream) throws IOException;

}
