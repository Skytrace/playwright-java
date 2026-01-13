package com.playwright.scrapper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.scrapper.model.CrawlTask;
import com.playwright.scrapper.util.PerformanceTracker;
import com.playwright.scrapper.util.ScrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BaseScrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseScrapper.class);
    private static final int MAX_DEPTH = 100;
    private ScrapperUtil scrapperUtil = new ScrapperUtil();
    // Результаты: URL -> Карта метрик
    private Map<String, Map<String, Double>> allPerformanceResults = new LinkedHashMap<>();
    // Реестр посещенных адресов
    private Set<String> visitedUrls = new HashSet<>();
    // Очередь для итеративного обхода (URL и его текущая глубина)
    private Map<String, Object> taskManager;
    private Queue<CrawlTask> queue = new LinkedList<>();
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

    //TODO add exception
    // if properties are not allow, do not start the crawler
    protected void init(Map<String, Object> tm) {
        if (tm != null && !tm.isEmpty()) {
            taskManager = new HashMap<>(tm);
            LOGGER.info("=== TASK MANAGER HAS BEEN INITIALIZED === ");
            LOGGER.info("Here is following tasks: {}", taskManager);
        }
    }

    protected void startCrawl() {
        BrowserContext context = getBrowserContext();
        if (context == null) return;

        Page page = context.newPage();
        String startUrl = "https://" + taskManager.get("domain");

        // Добавляем стартовую страницу в очередь
        queue.add(new CrawlTask(startUrl, 0));

        // Итеративный цикл
        while (!queue.isEmpty()) {
            CrawlTask task = queue.poll();

            if (task.depth() > MAX_DEPTH || visitedUrls.contains(task.url())) {
                continue;
            }

            processPage(page, task.url(), task.depth());
        }

        printFinalReport();

        closeAll();
        LOGGER.info("Deep research completed. Total unique pages analyzed: {}", visitedUrls.size());
    }

    private void processPage(Page page, String url, int depth) {
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

            String foundPhrase = "Ballerina";
            // поиск по слову
            boolean isTextPresents = page.getByText("Ballerina").isVisible();

            if (isTextPresents) {
                LOGGER.info("=== [FOUND] === | Text '{}' found in page: {}", foundPhrase, page.url());
            }

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
            Set<Link> internalLinksOnly = scrapperUtil.filterInternalLinks(uniqueLinks, String.valueOf(taskManager.get("domain")));
            Set<Link> finalLinks = scrapperUtil.aggregateInternalLinks(internalLinksOnly, String.valueOf(taskManager.get("domain")));

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

    private void printFinalReport() {
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

    protected static void closeAll() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        LOGGER.info("Playwright resources closed");
    }

}
