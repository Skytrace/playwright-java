package com.playwright.scrapper.util;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.scrapper.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ScrapperAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapperAPI.class);
    private final ScrapperUtil scrapperUtil = new ScrapperUtil();
    // Результаты: URL -> Карта метрик
    private final Map<String, Map<String, Double>> allPerformanceResults = new LinkedHashMap<>();
    // Реестр посещенных адресов
    private final Set<String> visitedUrls = new HashSet<>();
    // Очередь для итеративного обхода (URL и его текущая глубина)
    private final Queue<CrawlTask> queue = new LinkedList<>();

    private record CrawlTask(String url, int depth) {}

    public void processPage(Page page, String url, int depth, String domain) {
        LOGGER.info(">>> [Depth {}] Processing: {}", depth, url);
        visitedUrls.add(url);

        try {
            // 1. Переход на страницу
            page.navigate(url);
            page.waitForLoadState();

            // 2. Сбор метрик
            Map<String, Double> metrics = PerformanceTracker.getPageMetrics(page);
            allPerformanceResults.put(url, metrics);
            PerformanceTracker.logMetrics(page, metrics);

            // 3. Сбор ссылок
            List<Link> rawLinks = new ArrayList<>();
            page.getByRole(AriaRole.LINK).elementHandles().forEach(e -> {
                try {
                    rawLinks.add(new Link(e.innerText(), e.getAttribute("href")));
                } catch (Exception ignored) {}
            });

            // 4. Фильтрация (логика ScrapperUtil)
            List<Link> linksWithoutText = scrapperUtil.removeLinksWithoutText(rawLinks);
            List<Link> linksWithoutSharpSymbol = scrapperUtil.removeLinksWithSharpSymbol(linksWithoutText);
            Set<Link> uniqueLinks = scrapperUtil.removeDuplicationLinks(linksWithoutSharpSymbol);
            Set<Link> internalLinksOnly = scrapperUtil.filterInternalLinks(uniqueLinks, domain);
            Set<Link> finalLinks = scrapperUtil.aggregateInternalLinks(internalLinksOnly, domain);

            LOGGER.info("Found {} internal links on page {}", finalLinks.size(), url);

            // 5. Добавление найденных ссылок в очередь для следующих итераций
            for (Link nextLink : finalLinks) {
                if (!visitedUrls.contains(nextLink.link())) {
                    queue.add(new CrawlTask(nextLink.link(), depth + 1));
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error processing {}: {}", url, e.getMessage());
        }
    }

    public void printFinalReport() {
        LOGGER.info("====== FINAL PERFORMANCE REPORT ======");
        if (allPerformanceResults.isEmpty()) {
            LOGGER.warn("No data collected.");
        } else {
            allPerformanceResults.forEach((url, metrics) -> {
                LOGGER.info("Page: {}", url);
                metrics.forEach((k, v) -> LOGGER.info("  >> {}: {} ms", k, String.format("%.2f", v)));
            });
        }
    }
}
