package com.example.crawler.service;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import com.example.crawler.model.CrawledPage;
import com.example.crawler.model.KnowledgeDocument;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

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
        
        try(Playwright playwright = Playwright.create()) {
        	
        	Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        	
        	BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        			.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        			.setExtraHTTPHeaders(java.util.Map.of("Referer", "https://google.com")));
        	
        	Page page = context.newPage();
        
        
	        while (!urlsToVisit.isEmpty()
	                && visitedUrls.size() < maxPages) {
	
	            String currentUrl = urlsToVisit.poll();
	
	            if (visitedUrls.contains(currentUrl)) {
	                continue;
	            }
	
	            try {
	
	                System.out.println("--------------------------------");
	                System.out.println("Crawling: " + currentUrl);
	             
	                page.navigate(currentUrl);
	                
	                page.waitForLoadState(LoadState.NETWORKIDLE);
	                
	                String fullyRenderedHtml = page.content();
	                
	                Document document = Jsoup.parse(fullyRenderedHtml, currentUrl);
                    document.setBaseUri(currentUrl);
	
	                
	
	                // Mark URL as visited
	                visitedUrls.add(currentUrl);
	
	                // Extract useful content
	                CrawledPage crawledPage =
	                        contentExtractorService.extract(document, currentUrl);
	
	                // Store the page
	                crawledPages.add(crawledPage);
	
	                System.out.println("Title: " + crawledPage.getTitle());
	
	                System.out.println("\nContent:");
	                for (String paragraph : crawledPage.getParagraphs()) {
	                    System.out.println("- " + paragraph);
	                }
	                
	                System.out.println("\nHeadings:");
	                for (String heading : crawledPage.getHeadings()) {
	                    System.out.println("- " + heading);
	                }
	
	
	                System.out.println("\nLists:");
	                for (String item : crawledPage.getLists()) {
	                    System.out.println("- " + item);
	                }
	
	                System.out.println("\nTables:");
	                for (String table : crawledPage.getTables()) {
	                    System.out.println("- " + table);
	                }
	
	                System.out.println("\nImages:");
	                for (String image : crawledPage.getImages()) {
	                    System.out.println("- " + image);
	                }
	
	                System.out.println("\nVideos:");
	                for (String video : crawledPage.getVideos()) {
	                    System.out.println("- " + video);
	                }
	
	                System.out.println("\nDocuments:");
	                for (String doc : crawledPage.getDocuments()) {
	                    System.out.println("- " + doc);
	                }
	
	                System.out.println("\nInternal Links:");
	                for (String link : crawledPage.getInternalLinks()) {
	                    System.out.println("- " + link);
	                }
	
	                System.out.println("\nExternal Links:");
	                for (String link : crawledPage.getExternalLinks()) {
	                    System.out.println("- " + link);
	                }
	                // Add internal links to queue
	                for (String nextUrl : crawledPage.getInternalLinks()) {
	
	                    if (!visitedUrls.contains(nextUrl)
	                            && !urlsToVisit.contains(nextUrl)) {
	
	                        urlsToVisit.add(nextUrl);
	                    }
	                }
	
	            } catch (Exception e) {
	
	                System.out.println(
	                        "Failed to crawl: " + currentUrl);
	
	                // Mark failed URL as visited
	                visitedUrls.add(currentUrl);
	            }
	            
	        }
        
	        browser.close();
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

