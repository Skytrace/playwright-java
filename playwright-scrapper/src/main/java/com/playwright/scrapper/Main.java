package com.playwright.scrapper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import java.util.*;

import com.playwright.scrapper.util.ScrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends BaseScrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String DOMAIN = "wccftech.com";
    private ScrapperUtil scrapperUtil = new ScrapperUtil();

    public BrowserContext initPlayWright() {
        return getBrowserContext();
    }

    public static void main(String[] args) {
        LOGGER.info("Started to research the web...");

        Main main = new Main();
        BrowserContext playWrightContext = main.initPlayWright();
        Page page = playWrightContext.newPage();
        page.navigate("https://" + DOMAIN);

        List<Link> actualLinks = new ArrayList<>();
        page.getByRole(AriaRole.LINK).elementHandles().stream()
                .forEach(e -> {
                    actualLinks.add(new Link(e.innerText(), e.getAttribute("href")));
                });
        main.scrapperUtil.printFoundLinks(actualLinks);

        List<Link> filteredLinksWithoutText = main.scrapperUtil.removeLinksWithoutText(actualLinks);
        main.scrapperUtil.printFoundLinks(filteredLinksWithoutText);

        List<Link> filteredLinksWithoutSharpSymbol = main.scrapperUtil.removeLinksWithSharpSymbol(filteredLinksWithoutText);
        main.scrapperUtil.printFoundLinks(filteredLinksWithoutSharpSymbol);

        List<Link> linksWithoutDuplications = main.scrapperUtil.removeDuplicationLinks(filteredLinksWithoutSharpSymbol);
        LOGGER.info("====== THIS LIST OF UNIQUE LINK ADDRESSES FROM THE MAIN PAGE ======");
        main.scrapperUtil.printFoundLinks(linksWithoutDuplications);

        List<Link> filteredInternalLinksOnly = main.scrapperUtil.filterInternalLinks(linksWithoutDuplications, DOMAIN);
        LOGGER.info("====== THIS LIST OF INTERNAL LINKS THE MAIN PAGE ======");
        main.scrapperUtil.printFoundLinks(filteredInternalLinksOnly);

        List<Link> aggregationInternalLinks = main.scrapperUtil.aggregateInternalLinks(filteredInternalLinksOnly, DOMAIN);
        LOGGER.info("====== THIS FINAL AGGREGATION LINKS LIST FROM THE MAIN PAGE ======");
        main.scrapperUtil.printFoundLinks(aggregationInternalLinks);

        closeAll();
        LOGGER.info("Research the web has been complete");
    }

}

