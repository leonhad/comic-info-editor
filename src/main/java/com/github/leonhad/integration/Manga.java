package com.github.leonhad.integration;

import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.List;

@Getter
@Setter
public class Manga {

    private Integer id;
    private String title;
    private URL coverImage;
    private String description;
    private Integer volumes;
    private Integer chapters;
    private List<String> genres;
    private List<String> tags;
    private String format;
    private Integer startDay;
    private Integer startMonth;
    private Integer startYear;
    private Double averageScore;
    private List<String> characters;
    private List<Staff> staff;
    private URL siteUrl;
    private List<Link> externalLinks;

}
