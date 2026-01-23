package com.playwright.scrapper.model.report;

import java.util.List;
import java.util.Map;

public record PageReport(
        String page,
        PerformanceInfo performanceInfo,
        Map<String, Integer> foundPhrases,
        SeoInfo seoInfo,
        List<Image> images) {
}
