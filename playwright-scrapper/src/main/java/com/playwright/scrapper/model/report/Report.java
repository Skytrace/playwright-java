package com.playwright.scrapper.model.report;

import java.util.List;

public record Report(
        List<PageReport> pagesReport
) {
}
