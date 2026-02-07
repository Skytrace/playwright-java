# Web Scraper Pro 🚀

**Web Scraper Pro** is a professional-grade pet project designed for deep website analysis. 
It combines the capabilities of a search crawler, SEO auditor, and performance monitor. 
Built with Java Spring Boot and Playwright, it accurately processes modern SPAs 
(Single Page Applications) and JavaScript-heavy websites.

---

## 🎯 Project Core Objective

The primary goal is to provide developers and SEO specialists with a detailed report on a web resource's health. Unlike simple parsers, this tool simulates real user behavior in a browser(without cached files), gathering precise loading metrics and analyzing visible content.

### Key Features

- **Deep Crawling**  
  Configurable scanning depth for automatic navigation through internal links.

- **SEO Audit**  
  Automatic collection of headings (H1–H6), meta-tags (keywords, description), and Page Title validation.

- **Performance Monitoring**  
  Measures DNS Lookup, TTFB, DOM Content Loaded, and Full Page Load metrics.

- **Keyword Tracking**  
  Counts occurrences of specific words or phrases on every scanned page.

- **Heavy Resource Optimization**  
  Smart request filtering (blocking ads and trackers) for stable operation on large portals like BBC or CNN.

---

## 🛠 Tech Stack

- **Backend:** Java 21, Spring Boot 3.2.1
- **Engine:** Microsoft Playwright (Chromium) — for rendering and network interaction
- **Frontend:** HTML5, Tailwind CSS, JavaScript
- **Analytics:** Navigation Timing API for gathering accurate performance data

---

## 💡 Use Cases

- **SEO Monitoring**  
  Quickly check meta-tags and heading structures across the entire site after a release or migration.

- **Performance Benchmarking**  
  Regular checks of page load speeds to identify bottlenecks (e.g., slow server response or heavy scripts).

- **Content Audit**  
  Search for brand mentions, verify mandatory legal texts, or track stop-words.

---

## 🚀 Getting Started

### Prerequisites
- JDK 21
- Maven

### Build
```bash
mvn clean install
```
### Run
```bash
mvn spring-boot:run
```

### Usage
```bash
http://localhost:8080
```

## 🛠 Implementation Details (Pro Tips)

The project implements Surgical Resource Blocking.
Instead of disabling JavaScript or images (which would distort performance metrics), the scraper selectively blocks only analytical and advertising domains (Google Analytics, DoubleClick, etc.).

This approach allows:
- Maintaining the visual integrity of the page
- Obtaining honest content rendering figures
- Avoiding timeouts on sites with infinite ad loading
