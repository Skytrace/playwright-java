package com.playwright.scrapper;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScraperController {
    private final ScrapperService scraperService;

    public ScraperController(ScrapperService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping("/")
    public String index() {
        return "index.html";
    }
}
