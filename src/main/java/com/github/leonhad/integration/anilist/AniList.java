package com.github.leonhad.integration.anilist;

import com.github.leonhad.exception.SearchException;
import com.github.leonhad.integration.Manga;
import com.github.leonhad.integration.MangaQuery;
import com.github.leonhad.integration.Site;
import com.github.leonhad.integration.Staff;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class AniList extends MangaQuery {

    @Override
    public List<Manga> search(String query) throws SearchException {
        String endpoint = "https://graphql.anilist.co";

        try (var search = AniList.class.getResourceAsStream("/anilist-search.graphql")) {
            assert search != null;
            var queryQl = IOUtils.toString(search, StandardCharsets.UTF_8);

            var q = new AniListQuery(queryQl, query);

            var url = new URL(endpoint);
            var connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            var gson = new Gson();

            // Send the request
            try (var os = connection.getOutputStream()) {
                var input = gson.toJson(q).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    var response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line.trim());
                    }

                    var data = gson.fromJson(response.toString(), HashMap.class);
                    return parseData((Map<String, Object>) data.get("data"));
                }
            } else {
                throw new SearchException("Error in AniList search. Error code: " + responseCode);
            }

        } catch (Exception e) {
            throw new SearchException(e);
        }
    }

    private List<Manga> parseData(Map<String, Object> data) {
        if (data.containsKey("Page")) {
            return parsePage((Map<String, Object>) data.get("Page"));
        }

        return List.of();
    }

    private List<Manga> parsePage(Map<String, Object> page) {
        var mangas = new ArrayList<Manga>();

        var medias = (Collection<Map<String, Object>>) page.get("media");
        if (medias != null) {
            for (var media : medias) {
                mangas.add(parseMedia(media));
            }
        }

        return mangas;
    }

    private Manga parseMedia(Map<String, Object> media) {
        var manga = new Manga();
        manga.setId(getInteger(media, "id"));
        manga.setTitle(getString((Map<String, Object>) media.get("title"), "english"));
        manga.setCoverImage(getURL((Map<String, Object>)media.get("coverImage"), "medium"));
        manga.setDescription(getString(media, "description"));
        manga.setVolumes(getInteger(media, "volumes"));
        manga.setChapters(getInteger(media, "chapters"));
        manga.setGenres((List<String>) media.get("genres"));
        manga.setTags(parseTags((List<Map<String, Object>>) media.get("tags")));
        manga.setFormat(getString(media, "format"));
        parseStartDate(manga, (Map<String, Object>) media.get("startDate"));
        manga.setAverageScore(getDouble(media, "averageScore"));
        manga.setCharacters(parseCharacters((Map<String, Object>) media.get("characters")));
        manga.setStaff(parseStaff((Map<String, Object>) media.get("staff")));
        manga.setSiteUrl(getURL(media, "siteUrl"));
        manga.setExternalLinks(parseExternalLink((List<Map<String, Object>>) media.get("externalLinks")));
        return manga;
    }

    private List<Site> parseExternalLink(List<Map<String, Object>> externalLinks) {
        return Optional.ofNullable(externalLinks)
                .map(link -> link.stream().map(x -> {
                    var site = new Site();
                    site.setIcon(getURL(x, "icon"));
                    site.setLanguage(getString(x, "language"));
                    site.setSite(getString(x, "site"));
                    site.setType(getString(x, "type"));
                    site.setUrl(getURL(x, "url"));
                    return site;
                }).collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    private List<Staff> parseStaff(Map<String, Object> staffs) {
        return Optional.ofNullable(staffs)
                .map(x -> (List<Map<String, Object>>) x.get("edges"))
                .map(maps -> maps.stream()
                        .map(x -> {
                            var role = getString(x, "role");
                            var node = (Map<String, Object>) x.get("node");
                            var name = (Map<String, Object>) node.get("name");
                            var full = getString(name, "full");

                            var staff = new Staff();
                            staff.setName(full);
                            staff.setRole(role);
                            return staff;
                        }).collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    private List<String> parseCharacters(Map<String, Object> characters) {
        if (characters != null) {
            return characters.entrySet().stream()
                    .flatMap(e -> ((List<Map<String, Object>>) e.getValue()).stream())
                    .flatMap(e -> e.entrySet().stream())
                    .flatMap(e -> ((Map<String, Object>) e.getValue()).entrySet().stream())
                    .map(Map.Entry::getValue)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    private void parseStartDate(Manga manga, Map<String, Object> startDate) {
        if (startDate != null) {
            manga.setStartYear(getInteger(startDate, "year"));
            manga.setStartMonth(getInteger(startDate, "month"));
            manga.setStartDay(getInteger(startDate, "day"));
        }
    }

    private List<String> parseTags(List<Map<String, Object>> tags) {
        if (tags != null) {
            return tags.stream()
                    .map(x -> x.get("name"))
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
