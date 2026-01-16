package com.playwright.scrapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scraper")
public class ScraperController {
    private final ScraperService scraperService;

    public ScraperController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping("/start")
    public String startScraping(@RequestParam String domain) {
        new Thread(() -> scraperService.startCrawl(domain)).start();
        return "Scraping process started for domain: " + domain + ". Check console for results.";
    }
}
