package com.playwright.scrapper.model;

public record ScrapperRequest(String domain, String searchPhrase, Integer depth, boolean isTimeLoad) {}
