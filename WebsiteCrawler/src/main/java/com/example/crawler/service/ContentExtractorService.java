package com.example.crawler.service;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.example.crawler.model.CrawledPage;

@Service
public class ContentExtractorService {

    public CrawledPage extract(Document document, String url) {

        CrawledPage page = new CrawledPage();

        page.setUrl(url);
        page.setTitle(document.title());

        // Remove unnecessary elements
        document.select("script, style, noscript, nav, footer").remove();

        // Headings
        Elements headings = document.select("h1, h2, h3, h4, h5, h6");

        for (Element heading : headings) {

            String text = heading.text().trim();

            if (!text.isEmpty()) {
                page.getHeadings().add(text);
            }
        }

        // Paragraphs
        Elements paragraphs = document.select("main p, article p, section p");

        if (paragraphs.isEmpty()) {
            paragraphs = document.select("p");
        }

        for (Element paragraph : paragraphs) {

            String text = paragraph.text().trim();

            if (!text.isEmpty() && text.length() > 20) {

                // Split paragraph into sentences
                String[] sentences = text.split("(?<=[.!?])\\s+");

                for (String sentence : sentences) {

                    sentence = sentence.trim();

                    if (!sentence.isEmpty()) {
                        page.getParagraphs().add(sentence);
                    }
                }
            }
        }

        // Lists
        Elements lists = document.select("ul li, ol li");

        for (Element list : lists) {

            String text = list.text().trim();

            if (!text.isEmpty()) {
                page.getLists().add(text);
            }
        }

        // Tables
        Elements tables = document.select("table");

        for (Element table : tables) {

            String text = table.text().trim();

            if (!text.isEmpty()) {
                page.getTables().add(text);
            }
        }

        // Images
        Elements images = document.select("img[src]");

        for (Element image : images) {

            String imageUrl = image.absUrl("src");

            if (!imageUrl.isEmpty()) {
                page.getImages().add(imageUrl);
            }
        }

        // Videos
        Elements videos = document.select("video[src], video source[src], iframe[src]");

        for (Element video : videos) {

            String videoUrl;

            if (video.tagName().equals("source")) {
                videoUrl = video.absUrl("src");
            } else {
                videoUrl = video.absUrl("src");
            }

            if (!videoUrl.isEmpty()) {
                page.getVideos().add(videoUrl);
            }
        }

        // Documents
        Elements links = document.select("a[href]");

        for (Element link : links) {

            String linkUrl = link.absUrl("href");

            if (linkUrl.isEmpty()) {
                continue;
            }

            String lowerUrl = linkUrl.toLowerCase();

            if (lowerUrl.endsWith(".pdf")
                    || lowerUrl.endsWith(".doc")
                    || lowerUrl.endsWith(".docx")
                    || lowerUrl.endsWith(".xls")
                    || lowerUrl.endsWith(".xlsx")
                    || lowerUrl.endsWith(".ppt")
                    || lowerUrl.endsWith(".pptx")) {

                page.getDocuments().add(linkUrl);
            }
        }

        // Internal and external links
        String startHost = document.baseUri();

        try {

            java.net.URI baseUri = java.net.URI.create(startHost);

            Set<String> internalSet = new HashSet<>();
            Set<String> externalSet = new HashSet<>();

            for (Element link : links) {

                String linkUrl = link.absUrl("href");

                if (linkUrl.isEmpty()) {
                    continue;
                }

                try {

                    java.net.URI linkUri = java.net.URI.create(linkUrl);

                    if (baseUri.getHost() != null
                            && baseUri.getHost().equalsIgnoreCase(linkUri.getHost())) {

                        internalSet.add(linkUrl);

                    } else {

                        externalSet.add(linkUrl);
                    }

                } catch (Exception ignored) {
                }
            }

            page.getInternalLinks().addAll(internalSet);
            page.getExternalLinks().addAll(externalSet);

        } catch (Exception ignored) {
        }

        return page;
    }
}
