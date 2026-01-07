package com.playwright.scrapper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.playwright.scrapper.util.ScrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends BaseScrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String MAIN_PAGE = "https://playwright.dev";
    private ScrapperUtil scrapperUtil = new ScrapperUtil();

    public BrowserContext initPlayWright() {
        return getBrowserContext();
    }

    public static void main(String[] args) {
        LOGGER.info("Started to research the web...");

        Main main = new Main();
        BrowserContext playWrightContext = main.initPlayWright();
        Page page = playWrightContext.newPage();
        page.navigate(MAIN_PAGE);

        List<Link> actualLinks = new ArrayList<>();
        page.getByRole(AriaRole.LINK).elementHandles().stream()
                .forEach(e -> {
                    actualLinks.add(new Link(e.innerText(), e.getAttribute("href")));
                });
        main.scrapperUtil.printFoundLinks(actualLinks);

        List<Link> filteredLinksWithoutText = main.scrapperUtil.removeLinksWithoutText(actualLinks);
        main.scrapperUtil.printFoundLinks(filteredLinksWithoutText);

        closeAll();

        LOGGER.info("Research the web has been complete");
    }

}

