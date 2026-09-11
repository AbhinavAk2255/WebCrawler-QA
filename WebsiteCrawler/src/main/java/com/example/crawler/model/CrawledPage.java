package com.example.crawler.model;

import java.util.ArrayList;
import java.util.List;

public class CrawledPage {

    private String url;
    private String title;

    private List<String> headings = new ArrayList<>();
    private List<String> paragraphs = new ArrayList<>();
    private List<String> lists = new ArrayList<>();
    private List<String> tables = new ArrayList<>();

    private List<String> images = new ArrayList<>();
    private List<String> videos = new ArrayList<>();
    private List<String> documents = new ArrayList<>();

    private List<String> internalLinks = new ArrayList<>();
    private List<String> externalLinks = new ArrayList<>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getHeadings() {
        return headings;
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }

    public List<String> getLists() {
        return lists;
    }

    public List<String> getTables() {
        return tables;
    }

    public List<String> getImages() {
        return images;
    }

    public List<String> getVideos() {
        return videos;
    }

    public List<String> getDocuments() {
        return documents;
    }

    public List<String> getInternalLinks() {
        return internalLinks;
    }

    public List<String> getExternalLinks() {
        return externalLinks;
    }
}