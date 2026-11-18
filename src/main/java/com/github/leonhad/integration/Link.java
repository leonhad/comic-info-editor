package com.github.leonhad.integration;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.net.URL;

@Getter
@Setter
public class Link implements Serializable {

    private URL icon;
    private String language;
    private String site;
    private String type;
    private URL url;
}
