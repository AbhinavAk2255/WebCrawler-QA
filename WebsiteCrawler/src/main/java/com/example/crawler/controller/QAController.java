package com.example.crawler.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.crawler.model.KnowledgeDocument;
import com.example.crawler.model.QuestionRequest;
import com.example.crawler.service.AIService;
import com.example.crawler.service.CrawlerService;
import com.example.crawler.service.KnowledgeDocumentService;

@RestController
public class QAController {

    private final CrawlerService crawlerService;
    private final KnowledgeDocumentService knowledgeDocumentService;
    private final AIService aiService;

    public QAController(
            CrawlerService crawlerService,
            KnowledgeDocumentService knowledgeDocumentService,
            AIService aiService) {

        this.crawlerService = crawlerService;
        this.knowledgeDocumentService = knowledgeDocumentService;
        this.aiService = aiService;
    }

    @GetMapping(value = "/ask", produces = "text/plain")
    public String askFromBrowser(
            @RequestParam String url,
            @RequestParam String question,
            @RequestParam(defaultValue = "3") int maxPages) throws Exception {

        KnowledgeDocument document =
                crawlerService.crawl(url, maxPages);

        String knowledge =
                knowledgeDocumentService.convertToText(document);

        return aiService.askQuestion(knowledge, question);
    }
    
    
}


