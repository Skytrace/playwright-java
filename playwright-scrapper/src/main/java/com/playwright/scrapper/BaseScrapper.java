package com.playwright.scrapper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class BaseScrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseScrapper.class);

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
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        LOGGER.info("Playwright resources closed");
    }

}
