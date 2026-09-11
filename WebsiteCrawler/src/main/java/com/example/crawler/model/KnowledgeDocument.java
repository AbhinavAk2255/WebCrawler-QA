package com.example.crawler.model;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeDocument {

    private String websiteUrl;
    private int totalPages;
    private List<CrawledPage> pages = new ArrayList<>();

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<CrawledPage> getPages() {
        return pages;
    }

    public void setPages(List<CrawledPage> pages) {
        this.pages = pages;
    }
}