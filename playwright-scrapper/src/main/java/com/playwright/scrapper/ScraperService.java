package com.playwright.scrapper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.scrapper.model.CrawlTask;
import com.playwright.scrapper.model.Link;
import com.playwright.scrapper.util.PerformanceTracker;
import com.playwright.scrapper.util.ScrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
class ScraperService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperService.class);
    private static final int MAX_DEPTH = 10;
    private final ScrapperUtil scrapperUtil = new ScrapperUtil();

    public void startCrawl(String domain) {
        Set<String> visitedUrls = new HashSet<>();
        Queue<CrawlTask> queue = new LinkedList<>();
        Map<String, Map<String, Double>> allResults = new LinkedHashMap<>();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setHeadless(false)
                     .setChannel("chrome")
                     .setArgs(Arrays.asList("--start-maximized")))) {

            LOGGER.info("Browser launched for domain: {}", domain);

            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
            Page page = context.newPage();

            String startUrl = "https://" + domain;
            queue.add(new CrawlTask(startUrl, 0));

            while (!queue.isEmpty()) {
                CrawlTask task = queue.poll();

                if (task.depth() > MAX_DEPTH || visitedUrls.contains(task.url())) {
                    continue;
                }

                processPage(page, task.url(), task.depth(), domain, visitedUrls, queue, allResults);
            }

            printFinalReport(allResults);
            LOGGER.info("Finished crawling domain: {}", domain);

        } catch (Exception e) {
            LOGGER.error("Scraper error for domain {}: {}", domain, e.getMessage());
        }
    }

    private void processPage(Page page, String url, int depth, String domain,
                             Set<String> visitedUrls, Queue<CrawlTask> queue,
                             Map<String, Map<String, Double>> results) {

        LOGGER.info(">>> [Depth {}] Processing: {}", depth, url);
        visitedUrls.add(url);

        try {
            page.navigate(url);
            page.waitForLoadState();

            Map<String, Double> metrics = PerformanceTracker.getPageMetrics(page);
            results.put(url, metrics);

            List<Link> rawLinks = new ArrayList<>();
            page.getByRole(AriaRole.LINK).elementHandles().forEach(e -> {
                try {
                    rawLinks.add(new Link(e.innerText(), e.getAttribute("href")));
                } catch (Exception ignored) {}
            });

            Set<Link> finalLinks = scrapperUtil.aggregateInternalLinks(
                    scrapperUtil.filterInternalLinks(new HashSet<>(rawLinks), domain),
                    domain
            );

            for (Link nextLink : finalLinks) {
                if (!visitedUrls.contains(nextLink.link())) {
                    queue.add(new CrawlTask(nextLink.link(), depth + 1));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error processing page {}: {}", url, e.getMessage());
        }
    }

    private void printFinalReport(Map<String, Map<String, Double>> results) {
        LOGGER.info("====== FINAL PERFORMANCE REPORT ======");
        results.forEach((url, metrics) -> {
            LOGGER.info("Page: {}", url);
            if (metrics != null) {
                metrics.forEach((k, v) -> LOGGER.info("  >> {}: {} ms", k, String.format("%.2f", v)));
            }
        });
    }
}