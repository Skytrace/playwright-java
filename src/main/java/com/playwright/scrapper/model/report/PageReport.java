package com.playwright.scrapper.model.report;


import java.util.Map;

public record PageReport(
        PerformanceInfo performanceInfo,
        Map<String, Long> searchPhrases,
        SeoInfo seoInfo) {
}
