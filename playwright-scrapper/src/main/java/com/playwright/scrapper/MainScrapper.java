package com.playwright.scrapper;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.playwright.scrapper.util.ScrapperAPI;
import com.playwright.scrapper.util.ScrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MainScrapper extends BaseScrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainScrapper.class);
    private static final String DOMAIN = "andreysobolev.art";
    private static final int MAX_DEPTH = 100;

    private final ScrapperAPI scrapperAPI = new ScrapperAPI();

    // Результаты: URL -> Карта метрик
    private final Map<String, Map<String, Double>> allPerformanceResults = new LinkedHashMap<>();
    // Реестр посещенных ссылок
    private final Set<String> visitedUrls = new HashSet<>();
    // Очередь для итеративного обхода (URL и его текущая глубина)
    private final Queue<CrawlTask> queue = new LinkedList<>();

    private record CrawlTask(String url, int depth) {}

    public static void main(String[] args) {
        LOGGER.info("Started deep research of the domain: {}", DOMAIN);
        MainScrapper main = new MainScrapper();
        main.startCrawl();
    }

    public void startCrawl() {
        BrowserContext context = getBrowserContext();
        if (context == null) return;

        Page page = context.newPage();
        String startUrl = "https://" + DOMAIN;

        // Добавляем стартовую страницу в очередь
        queue.add(new CrawlTask(startUrl, 0));

        // Итеративный цикл вместо рекурсии
        while (!queue.isEmpty()) {
            CrawlTask task = queue.poll();

            if (task.depth > MAX_DEPTH || visitedUrls.contains(task.url)) {
                continue;
            }

            scrapperAPI.processPage(page, task.url, task.depth, DOMAIN);
        }

        scrapperAPI.printFinalReport();

        closeAll();
        LOGGER.info("Deep research completed. Total unique pages analyzed: {}", visitedUrls.size());
    }

}