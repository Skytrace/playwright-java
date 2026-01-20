package com.playwright.scrapper.model;

public record ScrapperRequest(String domain, Integer depth, boolean isTimeLoad) {}
