package com.example.crawler.service;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import com.example.crawler.model.CrawledPage;
import com.example.crawler.model.KnowledgeDocument;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;

@Service
public class CrawlerService {

  private final ContentExtractorService contentExtractorService;

  public CrawlerService(ContentExtractorService contentExtractorService) {
    this.contentExtractorService = contentExtractorService;
  }

  public KnowledgeDocument crawl(String startUrl, int maxPages) {
    Queue<String> urlsToVisit = new ArrayDeque<>();
    Set<String> visitedUrls = new HashSet<>();
    List<CrawledPage> crawledPages = new ArrayList<>();

    String start = normalizeUrl(startUrl);
    urlsToVisit.add(start);

    URI startUri = safeUri(start);
    String startHost = startUri != null ? startUri.getHost() : null;

    try (Playwright playwright = Playwright.create()) {

      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
          .setHeadless(true)
          .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"))
      );

      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
          .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
          .setExtraHTTPHeaders(Map.of("Referer", "https://google.com"))
          .setIgnoreHTTPSErrors(true)
      );

      context.setDefaultNavigationTimeout(30_000);
      context.setDefaultTimeout(30_000);

      // Save RAM/bandwidth
      context.route("**/*", route -> {
        String type = route.request().resourceType();
        if ("image".equals(type) || "media".equals(type) || "font".equals(type)) route.abort();
        else route.resume();
      });

      while (!urlsToVisit.isEmpty() && visitedUrls.size() < maxPages) {
        String currentUrl = normalizeUrl(urlsToVisit.poll());
        if (visitedUrls.contains(currentUrl)) continue;

        // same-host crawl (recommended for “website crawler” behavior)
        if (startHost != null) {
          URI u = safeUri(currentUrl);
          if (u != null && u.getHost() != null && !startHost.equalsIgnoreCase(u.getHost())) {
            visitedUrls.add(currentUrl);
            continue;
          }
        }

        System.out.println("--------------------------------");
        System.out.println("Crawling: " + currentUrl);

        Page page = null;
        try {
          page = context.newPage();

          page.navigate(currentUrl, new Page.NavigateOptions()
              .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
              .setTimeout(30_000)
          );

          // SPA-friendly: wait briefly for DOM to stabilize
          waitForDomToStabilize(page, 5_000);

          String html = page.content();
          Document document = Jsoup.parse(html, currentUrl);
          document.setBaseUri(currentUrl);

          visitedUrls.add(currentUrl);

          CrawledPage crawledPage = contentExtractorService.extract(document, currentUrl);
          crawledPages.add(crawledPage);

          // Prevent queue blow-up beyond maxPages
          for (String next : crawledPage.getInternalLinks()) {
            if (visitedUrls.size() + urlsToVisit.size() >= maxPages) break;
            String n = normalizeUrl(next);
            if (!visitedUrls.contains(n) && !urlsToVisit.contains(n)) urlsToVisit.add(n);
          }

        } catch (Exception e) {
          System.out.println("Failed to crawl: " + currentUrl + " | " + e.getMessage());
          visitedUrls.add(currentUrl);
        } finally {
          if (page != null) page.close();
        }
      }

      context.close();
      browser.close();
    }

    KnowledgeDocument knowledgeDocument = new KnowledgeDocument();
    knowledgeDocument.setWebsiteUrl(startUrl);
    knowledgeDocument.setTotalPages(crawledPages.size());
    knowledgeDocument.setPages(crawledPages);

    return knowledgeDocument;
  }

  private void waitForDomToStabilize(Page page, int maxMs) {
    long start = System.currentTimeMillis();
    int lastSize = -1;
    int stable = 0;

    while (System.currentTimeMillis() - start < maxMs) {
      int size = page.content().length();
      if (size == lastSize) stable++;
      else stable = 0;

      if (stable >= 3) return; // stable across ~3 checks
      lastSize = size;
      page.waitForTimeout(350);
    }
  }

  private URI safeUri(String url) {
    try { return URI.create(url); } catch (Exception e) { return null; }
  }

  // Remove fragments + drop tracking params to prevent infinite URL variants
  private String normalizeUrl(String url) {
    try {
      URI uri = URI.create(url.trim());
      String scheme = uri.getScheme();
      if (scheme == null) return url;

      String host = uri.getHost();
      if (host == null) return url;

      String path = (uri.getPath() == null || uri.getPath().isBlank()) ? "/" : uri.getPath();
      String query = filterQuery(uri.getRawQuery());

      // drop fragment always
      return new URI(scheme.toLowerCase(), null, host.toLowerCase(), uri.getPort(), path, query, null).toString();
    } catch (Exception e) {
      return url;
    }
  }

  private String filterQuery(String rawQuery) {
    if (rawQuery == null || rawQuery.isBlank()) return null;

    List<String> kept = new ArrayList<>();
    for (String part : rawQuery.split("&")) {
      if (part.isBlank()) continue;

      String key = part;
      int eq = part.indexOf('=');
      if (eq >= 0) key = part.substring(0, eq);

      String decodedKey;
      try {
        decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);
      } catch (Exception e) {
        decodedKey = key;
      }

      String k = decodedKey.toLowerCase();

      // drop common trackers + session-ish params
      if (k.startsWith("utm_") ||
          k.equals("gclid") || k.equals("fbclid") ||
          k.equals("transaction_id") || k.equals("sessionid") ||
          k.equals("ref") || k.equals("referrer")) {
        continue;
      }

      kept.add(part);
    }

    if (kept.isEmpty()) return null;
    return String.join("&", kept);
  }
}