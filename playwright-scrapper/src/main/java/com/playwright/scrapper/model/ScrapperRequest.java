package com.playwright.scrapper.model;

import java.util.List;

public record ScrapperRequest(String domain, List<String> searchPhrases, Integer depth, boolean isTimeLoad) {}
