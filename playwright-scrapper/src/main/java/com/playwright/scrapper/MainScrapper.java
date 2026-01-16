package com.playwright.scrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class MainScrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainScrapper.class);
    private static final String DOMAIN = "andreysobolev.art";
    private static Map<String, Object> taskManager = new HashMap<>();

    {
        taskManager.put("domain", "andreysobolev.art");
        taskManager.put("depth", 10L);
        taskManager.put("showListPageByLoadTime", true);
        taskManager.put("isByDesc", true);
        taskManager.put("searchPhrase", "ballerina");
        taskManager.put("isDisplaySeo", true);
        taskManager.put("displaySeo", new HashMap<>(
                Map.of(
                        "isTitle", true,
                        "isImgAlt", true,
                        "isMetaName", true))
        );
    }

    public static void main(String[] args) {
        LOGGER.info("Started deep research of the domain: {}", DOMAIN);
        MainScrapper main = new MainScrapper();
    }

}