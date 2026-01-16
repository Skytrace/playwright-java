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
    private static final int MAX_DEPTH = 3;
    private final ScrapperUtil scrapperUtil = new ScrapperUtil();
    private static Playwright playwright;
    private static Browser browser;

    protected static BrowserContext getBrowserContext() {
        try {
            playwright = Playwright.create();
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setChannel("chrome")
                    .setArgs(Arrays.asList("--start-maximized"));
            browser = playwright.chromium().launch(launchOptions);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(null));

            LOGGER.info("Playwright browser context initialized");
            return context;
        } catch (Exception e) {
            LOGGER.error("Playwright browser context isn't initialized due to: {}", e.getMessage());
            closeAll();
            return null;
        }
    }

    protected static void closeAll() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
        LOGGER.info("Playwright resources closed");
    }

    public void startCrawl(String domain) {
        Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
        Queue<CrawlTask> queue = new LinkedList<>();
        Map<String, Map<String, Double>> allResults = new LinkedHashMap<>();

        BrowserContext context = getBrowserContext();
        if (context == null) return;

        try {
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
        } finally {
            closeAll();
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

            // Сбор ссылок
            List<Link> rawLinks = new ArrayList<>();
            page.getByRole(AriaRole.LINK).elementHandles().forEach(e -> {
                try {
                    rawLinks.add(new Link(e.innerText(), e.getAttribute("href")));
                } catch (Exception ignored) {}
            });

            List<Link> linksWithoutText = scrapperUtil.removeLinksWithoutText(rawLinks);
            List<Link> linksWithoutSharp = scrapperUtil.removeLinksWithSharpSymbol(linksWithoutText);
            Set<Link> uniqueLinks = scrapperUtil.removeDuplicationLinks(linksWithoutSharp);
            Set<Link> internalOnly = scrapperUtil.filterInternalLinks(uniqueLinks, domain);
            Set<Link> finalLinks = scrapperUtil.aggregateInternalLinks(internalOnly, domain);

            LOGGER.info("Found {} internal links on page {}", finalLinks.size(), url);

            for (Link nextLink : finalLinks) {
                if (!visitedUrls.contains(nextLink.link())) {
                    queue.add(new CrawlTask(nextLink.link(), depth + 1));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error processing {}: {}", url, e.getMessage());
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