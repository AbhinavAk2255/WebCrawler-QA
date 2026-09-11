package com.example.crawler.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.crawler.model.KnowledgeDocument;
import com.example.crawler.service.CrawlerService;
import com.example.crawler.service.KnowledgeDocumentService;

@RestController
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final KnowledgeDocumentService knowledgeDocumentService;

    public CrawlerController(
            CrawlerService crawlerService,
            KnowledgeDocumentService knowledgeDocumentService) {

        this.crawlerService = crawlerService;
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    @GetMapping("/crawl")
    public KnowledgeDocument crawl(
            @RequestParam String url,
            @RequestParam(defaultValue = "5") int maxPages) {

        return crawlerService.crawl(url, maxPages);
    }

    @GetMapping(value = "/crawl/text", produces = "text/plain")
    public String crawlAsText(

            @RequestParam String url,

            @RequestParam(defaultValue = "5") int maxPages) {

        KnowledgeDocument document =
                crawlerService.crawl(url, maxPages);

        return knowledgeDocumentService.convertToText(document);
    }
}
