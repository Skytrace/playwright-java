package com.playwright.scrapper.util;

import com.playwright.scrapper.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class ScrapperUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapperUtil.class);

    public void printFoundLinks(List<Link> links) {
        if (links.size() > 0) {
            LOGGER.info("=== BELOW LIST OF FOUND LINKS ===");
            links.forEach((link) -> LOGGER.info("'{}' - '{}'", link.name(), link.url()));
            LOGGER.info("=== THE LIST IS FINISHED ===");
            LOGGER.info("=== TOTAL COUNT: {} ===", links.size());
        } else {
            LOGGER.info("No links found");
        }
    }

    public List<Link> removeLinksWithoutText(List<Link> links) {
        LOGGER.info("=== REMOVING LINKS WITHOUT TEXT AND WHITESPACES");
        String whitespacesRegex = "^\\s+$";
        return links.stream()
                .filter(link -> !link.name().isEmpty() && !link.name().matches(whitespacesRegex))
                .toList();

    }
}
