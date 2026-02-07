package com.playwright.scrapper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.scrapper.model.CrawlTask;
import com.playwright.scrapper.model.Link;
import com.playwright.scrapper.model.ScrapperRequest;
import com.playwright.scrapper.model.report.*;
import com.playwright.scrapper.util.PerformanceTracker;
import com.playwright.scrapper.util.ScrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
class ScrapperService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapperService.class);
    private final ScrapperUtil scrapperUtil = new ScrapperUtil();

    public Map<String, PageReport> startCrawl(ScrapperRequest scrapperRequest) {
        Set<String> visitedUrls = new HashSet<>();
        Queue<CrawlTask> queue = new LinkedList<>();
        Map<String, PageReport> finalReport = new HashMap<>();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setHeadless(false)
                     .setChannel("chrome")
                     .setArgs(Arrays.asList("--start-maximized")))) {

            LOGGER.info("Browser launched for domain: {}", scrapperRequest.domain());

            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));

            // blocking ad services
            context.route("**/*", route -> {
                String url = route.request().url().toLowerCase();
                if (url.contains("google-analytics") || url.contains("doubleclick") ||
                        url.contains("adservice") || url.contains("facebook.net") ||
                        url.contains("adnxs") || url.contains("analytics")) {
                    route.abort();
                } else {
                    route.resume();
                }
            });

            Page page = context.newPage();

            String startUrl = scrapperRequest.domain().trim();

            if (startUrl.endsWith("/")) {
                startUrl = startUrl.substring(0, startUrl.length() - 1);
            }

            boolean hasProtocol = startUrl.startsWith("http");

            if (!hasProtocol) {
                startUrl = "https://" + startUrl;
            }

            queue.add(new CrawlTask(startUrl, 0));

            while (!queue.isEmpty()) {
                CrawlTask task = queue.poll();

                if (task.depth() > scrapperRequest.depth() || visitedUrls.contains(task.url())) {
                    continue;
                }

                processPage(page, task.url(), task.depth(), scrapperRequest, startUrl, visitedUrls, queue, finalReport);
            }

            // isTimeLoad
            // then present the report by desc in Full Page Load order
            if (scrapperRequest.isTimeLoad()) {
                finalReport = finalReport.entrySet().stream()
                                .sorted(Comparator.comparingDouble(e -> e.getValue().performanceInfo().fullPageLoad()))
                                        .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (a, b) -> a,
                                                LinkedHashMap::new
                                        )).reversed();

                printFinalReport(finalReport);
            } else {
                printFinalReport(finalReport);
            }
            LOGGER.info("Finished crawling domain: {}", scrapperRequest.domain());

        } catch (Exception e) {
            LOGGER.error("Scraper error for domain {}: {}", scrapperRequest.domain(), e.getMessage());
        }
         finally {
            return finalReport;
        }
    }

    private void processPage(Page page, String url, int depth, ScrapperRequest req, String startUrl,
                             Set<String> visitedUrls, Queue<CrawlTask> queue,
                             Map<String, PageReport> results) {

        LOGGER.info(">>> [Depth {}] Processing: {}", depth, url);
        visitedUrls.add(url);

        try {
            page.navigate(url, new Page.NavigateOptions().setTimeout(240000));
            page.waitForLoadState();

            // prepare page performance
            PerformanceInfo performanceInfo = PerformanceTracker.convertPageMetrics(PerformanceTracker.getPageMetrics(page));

            // count only visible search phrases
            List<String> searchPhrases = req.searchPhrases();
            Map<String, Long> foundPhrasesReport = new LinkedHashMap<>();
            searchPhrases.forEach(searchPhrase -> {
                if (searchPhrase != null && !searchPhrase.isEmpty()) {
                    Locator phrase = page.getByText(searchPhrase);
                    long onlyVisibleElements = phrase.elementHandles().stream().filter(e -> e.isVisible()).count();
                    foundPhrasesReport.put(searchPhrase, Long.valueOf(onlyVisibleElements));
                    LOGGER.info("Search phrase/word '{}' was found on current page", searchPhrase);
                }
            });


            SeoInfo seoInfo = null;
            if (req.isSeo()) {
                LOGGER.info("Search SEO info");

                String pageTitle = page.title();

                List<Image> images = page.getByRole(AriaRole.IMG).all().stream()
                        .filter(e -> e.isVisible())
                        .map(img -> new Image(img.getAttribute("src"), img.getAttribute("alt")))
                        .toList();

                List<String> metasKeywords = getMetaInfo("keywords", page);
                List<String> metasDescription = getMetaInfo("description", page);
                List<ParagraphHeaders> paragraphHeaders = page.locator("h1, h2, h3, h4, h5, h6").all().stream()
                        .map(header -> new ParagraphHeaders(
                                header.evaluate("el => el.tagName").toString(),
                                header.textContent()
                        ))
                        .toList();

                seoInfo = new SeoInfo(pageTitle, paragraphHeaders, metasKeywords, metasDescription);
                LOGGER.info("Analyzing SEO info from page '{}' completed");
            }

            // save page statistics
            results.put(url, new PageReport(performanceInfo, foundPhrasesReport, seoInfo));

            List<Link> rawLinks = new ArrayList<>();
            page.getByRole(AriaRole.LINK).elementHandles().forEach(e -> {
                try {
                    rawLinks.add(new Link(e.innerText(), e.getAttribute("href")));
                } catch (Exception ignored) {}
            });

            Set<Link> finalLinks = scrapperUtil.aggregateInternalLinks(
                    scrapperUtil.filterInternalLinks(new HashSet<>(rawLinks), startUrl),
                    startUrl
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

    private List<String> getMetaInfo(String meta, Page page) {
        Locator metaLocator = page.locator("head > meta[name='" + meta + "']");

        if (metaLocator.count() == 0) {
            return Collections.emptyList();
        }

        String content = metaLocator.first().getAttribute("content");
        if (content == null || content.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(content.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private void printFinalReport(Map<String, PageReport> results) {
        LOGGER.info("======================================");
        LOGGER.info("=                ***                 =");
        LOGGER.info("=      FINAL PERFORMANCE REPORT      =");
        LOGGER.info("=                ***                 =");
        LOGGER.info("======================================");
        results.forEach((url, metrics) -> {
            LOGGER.info("======================= SPECIFIC PAGE REPORT =======================");
            LOGGER.info("Page: {}", url);
            if (metrics != null) {
                LOGGER.info(">> *****       Page Load Performance Report       *****");
                LOGGER.info(">> DNS LookUp: {} ms", String.format("%.2f", metrics.performanceInfo().dnsLookUp()));
                LOGGER.info(">> TCP Connection: {} ms", String.format("%.2f", metrics.performanceInfo().tcpConnection()));
                LOGGER.info(">> Time To First Byte: {} ms", String.format("%.2f", metrics.performanceInfo().timeToFirstByte()));
                LOGGER.info(">> DOM Content Loaded: {} ms", String.format("%.2f", metrics.performanceInfo().domContentLoad()));
                LOGGER.info(">> Full Page Loaded: {} ms", String.format("%.2f", metrics.performanceInfo().fullPageLoad()));

                LOGGER.info(">> *****           Search Phrases Report          *****");
                Map<String, Long> phrases = metrics.searchPhrases();
                phrases.forEach((phrase, count) -> {
                    LOGGER.info(">> {}: found {} times", phrase, count);
                });

                if (metrics.seoInfo() != null) {
                    LOGGER.info(">> *****           SEO Info Report          *****");
                    LOGGER.info(">> Title: {}", metrics.seoInfo().title());

                    metrics.seoInfo().paragraphHeaders().stream().forEach(header -> {
                        LOGGER.info(">> tag: {}, value: {}", header.header(), header.value());
                    });

                    metrics.seoInfo().metasDescription().stream().forEach(meta -> {
                        LOGGER.info(">> meta: description: {}", meta.toString());
                    });

                    metrics.seoInfo().metasKeywords().stream().forEach(meta -> {
                        LOGGER.info(">> meta: keywords: {}", meta.toString());
                    });
                }
            }
            LOGGER.info("====================================================================");
        });
    }
}