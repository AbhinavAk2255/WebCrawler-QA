package com.example.crawler.service;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.example.crawler.model.CrawledPage;

@Service
public class ContentExtractorService {

  // caps to avoid memory blow-ups on modern sites
  private static final int MAX_HEADINGS = 200;
  private static final int MAX_PARAGRAPH_SENTENCES = 400;
  private static final int MAX_LIST_ITEMS = 400;
  private static final int MAX_TABLES = 50;
  private static final int MAX_IMAGES = 120;
  private static final int MAX_VIDEOS = 60;
  private static final int MAX_DOCS = 60;
  private static final int MAX_LINKS = 600;

  public CrawledPage extract(Document document, String url) {
    CrawledPage page = new CrawledPage();
    page.setUrl(url);
    page.setTitle(document.title());

    // --------- Extract links FIRST (before removing nav/footer) ----------
    Elements linkElements = document.select("a[href]");
    URI baseUri;
    try {
      baseUri = URI.create(url);
    } catch (Exception e) {
      baseUri = null;
    }

    Set<String> internal = new LinkedHashSet<>();
    Set<String> external = new LinkedHashSet<>();
    Set<String> docs = new LinkedHashSet<>();

    for (Element link : linkElements) {
      if (internal.size() + external.size() >= MAX_LINKS) break;

      String linkUrl = link.absUrl("href");
      if (linkUrl == null || linkUrl.isBlank()) continue;

      String lower = linkUrl.toLowerCase();

      // ignore non-navigational links
      if (lower.startsWith("mailto:") || lower.startsWith("tel:") || lower.startsWith("javascript:")) continue;
      if (linkUrl.equals("#") || lower.endsWith("/#")) continue;

      // documents
      if (lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx")
          || lower.endsWith(".xls") || lower.endsWith(".xlsx")
          || lower.endsWith(".ppt") || lower.endsWith(".pptx")) {
        if (docs.size() < MAX_DOCS) docs.add(linkUrl);
        continue;
      }

      // internal vs external
      try {
        URI linkUri = URI.create(linkUrl);
        if (baseUri != null && baseUri.getHost() != null && linkUri.getHost() != null
            && baseUri.getHost().equalsIgnoreCase(linkUri.getHost())) {
          internal.add(linkUrl);
        } else {
          external.add(linkUrl);
        }
      } catch (Exception ignored) {}
    }

    page.getInternalLinks().addAll(internal);
    page.getExternalLinks().addAll(external);
    page.getDocuments().addAll(docs);

    // --------- Now remove noisy elements for text extraction ----------
    document.select("script, style, noscript").remove();

    // For content extraction, removing nav/footer is fine
    document.select("nav, footer").remove();

    // Headings
    Elements headings = document.select("h1, h2, h3, h4, h5, h6");
    for (Element heading : headings) {
      if (page.getHeadings().size() >= MAX_HEADINGS) break;
      String text = heading.text().trim();
      if (!text.isEmpty()) page.getHeadings().add(text);
    }

    // Paragraphs (sentences)
    Elements paragraphs = document.select("main p, article p, section p");
    if (paragraphs.isEmpty()) paragraphs = document.select("p");

    for (Element paragraph : paragraphs) {
      if (page.getParagraphs().size() >= MAX_PARAGRAPH_SENTENCES) break;

      String text = paragraph.text().trim();
      if (text.isEmpty() || text.length() <= 20) continue;

      String[] sentences = text.split("(?<=[.!?])\\s+");
      for (String s : sentences) {
        if (page.getParagraphs().size() >= MAX_PARAGRAPH_SENTENCES) break;
        s = s.trim();
        if (!s.isEmpty()) page.getParagraphs().add(s);
      }
    }

    // Lists
    Elements lists = document.select("ul li, ol li");
    for (Element li : lists) {
      if (page.getLists().size() >= MAX_LIST_ITEMS) break;
      String text = li.text().trim();
      if (!text.isEmpty()) page.getLists().add(text);
    }

    // Tables
    Elements tables = document.select("table");
    for (Element table : tables) {
      if (page.getTables().size() >= MAX_TABLES) break;
      String text = table.text().trim();
      if (!text.isEmpty()) page.getTables().add(text);
    }

    // Images (only URLs; actual bytes not downloaded by Jsoup)
    Elements images = document.select("img[src]");
    for (Element img : images) {
      if (page.getImages().size() >= MAX_IMAGES) break;
      String imageUrl = img.absUrl("src");
      if (!imageUrl.isEmpty()) page.getImages().add(imageUrl);
    }

    // Videos/iframes
    Elements videos = document.select("video[src], video source[src], iframe[src]");
    for (Element v : videos) {
      if (page.getVideos().size() >= MAX_VIDEOS) break;
      String videoUrl = v.absUrl("src");
      if (!videoUrl.isEmpty()) page.getVideos().add(videoUrl);
    }

    return page;
  }
}