package com.stepik.pw;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Execution(ExecutionMode.CONCURRENT)
public class FinalParallelNavigationTest {
    
    // ТЕСТ 1
    @ParameterizedTest
    @ValueSource(strings = {
        "/",
        "/login", 
        "/dropdown",
        "/javascript_alerts",
        "/checkboxes", 
        "/hover",
        "/status_codes/200"
    })
    void testMultiplePages(String path) {
        System.out.println("Testing: " + path + " | Thread: " + Thread.currentThread().getId());
        
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            page.navigate("https://the-internet.herokuapp.com" + path);
            assertTrue(page.content().length() > 50);
            
            context.close();
            browser.close();
        }
    }
    
    // ТЕСТ 2
    @ParameterizedTest
    @ValueSource(strings = {"chromium", "firefox"})
    void testTwoBrowsers(String browserName) {
        System.out.println("Browser: " + browserName + " | Thread: " + Thread.currentThread().getId());
        
        try (Playwright playwright = Playwright.create()) {
            Browser browser = "firefox".equals(browserName)
                ? playwright.firefox().launch()
                : playwright.chromium().launch();
            
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            page.navigate("https://the-internet.herokuapp.com/login");
            assertTrue(page.content().contains("Login"));
            
            context.close();
            browser.close();
        }
    }
    
    // ТЕСТ 3
    @ParameterizedTest
    @CsvSource({
        "/login, chromium",
        "/dropdown, chromium",
        "/checkboxes, chromium", 
        "/login, firefox",
        "/dropdown, firefox"
    })
    void testCombination(String path, String browserName) {
        System.out.println("Testing " + path + " in " + browserName + 
                         " | Thread: " + Thread.currentThread().getId());
        
        try (Playwright playwright = Playwright.create()) {
            Browser browser = "firefox".equals(browserName)
                ? playwright.firefox().launch()
                : playwright.chromium().launch();
            
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            page.navigate("https://the-internet.herokuapp.com" + path);
            assertTrue(page.content().length() > 50);
            
            context.close();
            browser.close();
        }
    }
}