package com.playwright.scrapper.model.report;

import java.util.List;

public record SeoInfo(
        String title,
        List<ParagraphHeaders> paragraphHeaders,
        List<String> metasKeywords,
        List<String> metasDescription
) {
}
