package com.playwright.scrapper;

import com.playwright.scrapper.model.ScrapperRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScrapperRestController {
    private final ScrapperService scraperService;

    public ScrapperRestController(ScrapperService scraperService) {
        this.scraperService = scraperService;
    }

    @PostMapping("/api/scrapper/start")
    public String startScraping(@RequestBody ScrapperRequest request) {
        if (request.domain() == null || request.domain().isBlank()) {
            return "Ошибка: домен не указан в теле JSON.";
        }

        new Thread(() -> scraperService.startCrawl(request.domain())).start();
        return "Scraping process started for domain: " + request.domain() + ". Check console for results.";
    }

}
