package com.example.crawler.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import com.example.crawler.model.CrawledPage;
import com.example.crawler.model.KnowledgeDocument;

@Service
public class CrawlerService {

    private final ContentExtractorService contentExtractorService;

    public CrawlerService(ContentExtractorService contentExtractorService) {
        this.contentExtractorService = contentExtractorService;
    }

    public KnowledgeDocument crawl(String startUrl, int maxPages) {

        Queue<String> urlsToVisit = new LinkedList<>();
        Set<String> visitedUrls = new HashSet<>();
        List<CrawledPage> crawledPages = new ArrayList<>();

        urlsToVisit.add(startUrl);

        while (!urlsToVisit.isEmpty()
                && visitedUrls.size() < maxPages) {

            String currentUrl = urlsToVisit.poll();

            if (visitedUrls.contains(currentUrl)) {
                continue;
            }

            try {

                System.out.println("--------------------------------");
                System.out.println("Crawling: " + currentUrl);

                Document document = Jsoup.connect(currentUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .referrer("https://google.com")
                        .followRedirects(true)
                        .timeout(10000)
                        .get();

                document.setBaseUri(currentUrl);

                // Mark URL as visited
                visitedUrls.add(currentUrl);

                // Extract useful content
                CrawledPage page =
                        contentExtractorService.extract(document, currentUrl);

                // Store the page
                crawledPages.add(page);

                System.out.println("Title: " + page.getTitle());

                System.out.println("\nContent:");
                for (String paragraph : page.getParagraphs()) {
                    System.out.println("- " + paragraph);
                }
                
                System.out.println("\nHeadings:");
                for (String heading : page.getHeadings()) {
                    System.out.println("- " + heading);
                }


                System.out.println("\nLists:");
                for (String item : page.getLists()) {
                    System.out.println("- " + item);
                }

                System.out.println("\nTables:");
                for (String table : page.getTables()) {
                    System.out.println("- " + table);
                }

                System.out.println("\nImages:");
                for (String image : page.getImages()) {
                    System.out.println("- " + image);
                }

                System.out.println("\nVideos:");
                for (String video : page.getVideos()) {
                    System.out.println("- " + video);
                }

                System.out.println("\nDocuments:");
                for (String doc : page.getDocuments()) {
                    System.out.println("- " + doc);
                }

                System.out.println("\nInternal Links:");
                for (String link : page.getInternalLinks()) {
                    System.out.println("- " + link);
                }

                System.out.println("\nExternal Links:");
                for (String link : page.getExternalLinks()) {
                    System.out.println("- " + link);
                }
                // Add internal links to queue
                for (String nextUrl : page.getInternalLinks()) {

                    if (!visitedUrls.contains(nextUrl)
                            && !urlsToVisit.contains(nextUrl)) {

                        urlsToVisit.add(nextUrl);
                    }
                }

            } catch (IOException e) {

                System.out.println(
                        "Failed to crawl: " + currentUrl);

                // Mark failed URL as visited
                visitedUrls.add(currentUrl);
            }
        }

        // Create Knowledge Document
        KnowledgeDocument knowledgeDocument =
                new KnowledgeDocument();

        knowledgeDocument.setWebsiteUrl(startUrl);
        knowledgeDocument.setTotalPages(crawledPages.size());
        knowledgeDocument.setPages(crawledPages);

        System.out.println("--------------------------------");
        System.out.println("Crawling completed.");
        System.out.println("Pages successfully crawled: "
                + crawledPages.size());

        return knowledgeDocument;
    }
}

