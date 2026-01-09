package com.playwright.scrapper.util;

import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class PerformanceTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceTracker.class);

    public static Map<String, Double> getPageMetrics(Page page) {
        Object result = page.evaluate("() => {\n" +
                "  const [timing] = performance.getEntriesByType('navigation');\n" +
                "  if (!timing) return null;\n" +
                "  return {\n" +
                "    'DNS Lookup': timing.domainLookupEnd - timing.domainLookupStart,\n" +
                "    'TCP Connection': timing.connectEnd - timing.connectStart,\n" +
                "    'Time to First Byte (TTFB)': timing.responseStart - timing.requestStart,\n" +
                "    'DOM Content Loaded': timing.domContentLoadedEventEnd - timing.startTime,\n" +
                "    'Full Page Load': timing.loadEventEnd - timing.startTime\n" +
                "  };\n" +
                "}");

        Map<String, Double> metrics = new LinkedHashMap<>();

        if (result instanceof Map<?, ?> rawMetrics) {
            rawMetrics.forEach((key, value) -> {
                if (value instanceof Number num) {
                    metrics.put(key.toString(), num.doubleValue());
                }
            });
        }

        return metrics;
    }

    public static void logMetrics(Page page, Map<String, Double> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            LOGGER.warn("Performance metrics unavailable for page: {}", page.url());
            return;
        }

        LOGGER.info("--- Performance metrics for page: {}", page.url());
        metrics.forEach((name, value) -> {
            LOGGER.info("{}: {} ms", name, String.format("%.2f", value));
        });
    }
}
