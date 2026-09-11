package com.example.crawler.service;

import org.springframework.stereotype.Service;

import com.example.crawler.model.CrawledPage;
import com.example.crawler.model.KnowledgeDocument;

@Service
public class KnowledgeDocumentService {

    public String convertToText(KnowledgeDocument document) {

        StringBuilder text = new StringBuilder();

        text.append("==================================================\n");
        text.append("              WEBSITE INFORMATION\n");
        text.append("==================================================\n\n");

        text.append("Website: ")
                .append(document.getWebsiteUrl())
                .append("\n");

        text.append("Total Pages Crawled: ")
                .append(document.getTotalPages())
                .append("\n\n");

        int pageNumber = 1;

        for (CrawledPage page : document.getPages()) {

            text.append("--------------------------------------------------\n");
            text.append("Page ").append(pageNumber).append("\n");
            text.append("--------------------------------------------------\n\n");

            text.append("Page: ")
                    .append(page.getTitle())
                    .append("\n");

            text.append("URL: ")
                    .append(page.getUrl())
                    .append("\n\n");

            // HEADINGS
            if (!page.getHeadings().isEmpty()) {

                text.append("CONTENT\n\n");

                for (String heading : page.getHeadings()) {

                    if (!heading.isBlank()) {

                        text.append("# ")
                                .append(heading)
                                .append("\n\n");
                    }
                }
            }

            // PARAGRAPHS
            if (!page.getParagraphs().isEmpty()) {

                for (String paragraph : page.getParagraphs()) {

                    if (!paragraph.isBlank()) {

                        text.append(paragraph.trim())
                                .append("\n\n");
                    }
                }
            }

            // LISTS
            if (!page.getLists().isEmpty()) {

                text.append("LISTS\n\n");

                for (String item : page.getLists()) {

                    text.append("- ")
                            .append(item.trim())
                            .append("\n");
                }

                text.append("\n");
            }

            // TABLES
            if (!page.getTables().isEmpty()) {

                text.append("TABLES\n\n");

                for (String table : page.getTables()) {

                    text.append(table.trim())
                            .append("\n\n");
                }
            }

            // IMAGES
            if (!page.getImages().isEmpty()) {

                text.append("IMAGES\n\n");

                for (String image : page.getImages()) {

                    text.append("- ")
                            .append(image)
                            .append("\n");
                }

                text.append("\n");
            }

            // VIDEOS
            if (!page.getVideos().isEmpty()) {

                text.append("VIDEOS\n\n");

                for (String video : page.getVideos()) {

                    text.append("- ")
                            .append(video)
                            .append("\n");
                }

                text.append("\n");
            }

            // DOCUMENTS
            if (!page.getDocuments().isEmpty()) {

                text.append("DOCUMENTS\n\n");

                for (String doc : page.getDocuments()) {

                    text.append("- ")
                            .append(doc)
                            .append("\n");
                }

                text.append("\n");
            }

            // INTERNAL LINKS
            if (!page.getInternalLinks().isEmpty()) {

                text.append("INTERNAL LINKS\n\n");

                for (String link : page.getInternalLinks()) {

                    text.append("- ")
                            .append(link)
                            .append("\n");
                }

                text.append("\n");
            }

            // EXTERNAL LINKS
            if (!page.getExternalLinks().isEmpty()) {

                text.append("EXTERNAL LINKS\n\n");

                for (String link : page.getExternalLinks()) {

                    text.append("- ")
                            .append(link)
                            .append("\n");
                }

                text.append("\n");
            }

            pageNumber++;
        }

        return text.toString();
    }
}



