package com.github.leonhad.integration;

import java.net.URL;

public class Link {

    private URL icon;
    private String language;
    private String site;
    private String type;
    private URL url;

    public URL getIcon() {
        return icon;
    }

    public void setIcon(URL icon) {
        this.icon = icon;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
