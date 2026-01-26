package com.playwright.scrapper;

import com.playwright.scrapper.model.ScrapperRequest;
import com.playwright.scrapper.model.report.PageReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@RestController
public class ScrapperRestController {
    private final ScrapperService scraperService;

    public ScrapperRestController(ScrapperService scraperService) {
        this.scraperService = scraperService;
    }

    @PostMapping("/api/scrapper/start")
    public ResponseEntity<?> startScraping(@RequestBody ScrapperRequest request) {
        if (request.domain() == null || request.domain().isBlank()) {
            return ResponseEntity.badRequest().body("Error: domain missing in JSON body.");
        }
        try {
            Map<String, PageReport> report = scraperService.startCrawl(request);
            return ResponseEntity.ok(report);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Scraping failed: " + e.getMessage());
        }
    }


    @GetMapping("/version")
    public String getVersion() {
        try {
            return Files.readString(Paths.get("VERSION")).trim();
        } catch (IOException e) {
            return "unknown";
        }
    }
}
