package com.playwright.scrapper.util;

import com.playwright.scrapper.model.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScrapperUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapperUtil.class);

    public void printFoundLinks(Collection<Link> links) {
        if (links.size() > 0) {
            LOGGER.info("=== BELOW LIST OF FOUND LINKS ===");
            links.forEach((link) -> LOGGER.info("'{}' - '{}'", link.text(), link.link()));
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
                .filter(link -> !link.text().isEmpty() && !link.text().matches(whitespacesRegex))
                .toList();
    }

    public List<Link> removeLinksWithSharpSymbol(List<Link> links) {
        LOGGER.info("=== REMOVING LINKS WITH SHARP '#' SYMBOL");
        return links.stream()
                .filter(link -> !"#".equals(link.link().trim()))
                .toList();
    }

    public Set<Link> removeDuplicationLinks(List<Link> links) {
        LOGGER.info("=== REMOVING DUPLICATION LINKS");
        return links.stream().collect(Collectors.toSet());
    }

    public Set<Link> filterInternalLinks(Set<Link> links, String domain) {
        LOGGER.info("=== FILTERING INTERNAL LINKS ONLY ===");
        return links.stream()
                .filter(l -> l.link() != null && !l.link().isBlank())
                .filter(l -> {
                    String url = l.link().trim();

                    if (url.startsWith("/") && !url.startsWith("//")) {
                        return true;
                    }

                    try {
                        URI uri = new URI(url);
                        String host = uri.getHost();

                        if (host == null) {
                            return url.contains(domain);
                        }

                        return host.equals(domain) || host.endsWith("." + domain);

                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toSet());
    }

    public Set<Link> aggregateInternalLinks(Set<Link> links, String domain) {
        LOGGER.info("=== AGGREGATE INTERNAL LINKS ===");

        return links.stream()
                .map(link -> {
                    if (link.link().startsWith("/")) {
                        return new Link(link.text(), "https://" + domain + link.link()
                        );
                    }
                    return link;
                })
                .collect(Collectors.toSet());
    }

}