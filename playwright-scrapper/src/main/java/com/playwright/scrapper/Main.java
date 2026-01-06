package com.playwright.scrapper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setChannel("chrome")
                    .setArgs(Arrays.asList("--start-maximized"));
            Browser browser = playwright.chromium().launch(launchOptions);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(null));

            Page page = context.newPage();
            page.navigate("https://playwright.dev");

            Map<String, String> actualLinks = new HashMap<>();
            page.getByRole(AriaRole.LINK).elementHandles().stream()
                    .forEach(e -> {
                        actualLinks.put(e.innerText(), e.getAttribute("href"));
                    });
            printFoundLinks(actualLinks);
        }
    }

    public static void printFoundLinks(Map<String, String> map) {
        map.forEach((key, value) -> System.out.println(key + " = " + value));
    }
}

