package com.playwright.scrapper.model.report;

public record PerformanceInfo(
        Double dnsLookUp,
        Double tcpConnection,
        Double timeToFirstByte,
        Double domContentLoad,
        Double fullPageLoad
) {
}
