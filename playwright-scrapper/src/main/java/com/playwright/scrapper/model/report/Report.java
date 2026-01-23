package com.playwright.scrapper.model.report;

import java.util.List;
import java.util.Map;

public record Report(
        Map<String, PageReport> pageReports
) {
}
