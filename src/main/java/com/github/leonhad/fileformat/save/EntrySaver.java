package com.github.leonhad.fileformat.save;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntrySaver {

    private String name;

    private EntryConsumer consumer;

}
