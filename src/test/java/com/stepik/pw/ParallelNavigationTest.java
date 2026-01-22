package com.stepik.pw;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Execution(ExecutionMode.CONCURRENT)
public class ParallelNavigationTest {
    
    // Простая проверка загрузки страниц без требования к title
    @ParameterizedTest(name = "Load page: {0}")
    @ValueSource(strings = {
        "/",
        "/login", 
        "/dropdown",
        "/javascript_alerts",
        "/checkboxes",
        "/hover",
        "/status_codes/200",
        "/status_codes/404",
        "/status_codes/500"
    })
    void testPageNavigation(String path) {
        System.out.println("Thread " + Thread.currentThread().getId() + 
                         " loading: " + path);
        
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true));
            
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            String url = "https://the-internet.herokuapp.com" + path;
            page.navigate(url);
            
            // Ждем загрузки
            page.waitForLoadState();
            
            // Проверяем что URL изменился
            assertTrue(page.url().contains(url), 
                      "URL should contain: " + url);
            
            // Проверяем что страница загрузилась (любой контент есть)
            String html = page.content();
            assertNotNull(html, "HTML should not be null");
            assertTrue(html.length() > 50, 
                      "Page should have some content");
            
            context.close();
            browser.close();
        }
    }
    
    // Параметризация по браузерам
    @ParameterizedTest(name = "Test in {0}")
    @ValueSource(strings = {"chromium", "firefox"})
    void testBrowserCompatibility(String browserName) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser;
            
            if ("firefox".equals(browserName)) {
                browser = playwright.firefox().launch(new BrowserType.LaunchOptions()
                        .setHeadless(true));
            } else {
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setHeadless(true));
            }
            
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            // Тестируем разные страницы в разных браузерах
            page.navigate("https://the-internet.herokuapp.com/login");
            assertTrue(page.content().contains("Login"), 
                      "Login page should contain 'Login' in " + browserName);
            
            page.navigate("https://the-internet.herokuapp.com/dropdown");
            assertTrue(page.content().contains("Dropdown"), 
                      "Dropdown page should contain 'Dropdown' in " + browserName);
            
            context.close();
            browser.close();
        }
    }
    
    // Комбинированный тест с явными проверками
    @ParameterizedTest(name = "{1} on {0}")
    @CsvSource({
        "chromium, /login, Login Page",
        "chromium, /dropdown, Dropdown List",
        "chromium, /checkboxes, Checkboxes",
        "firefox, /login, Login Page",
        "firefox, /javascript_alerts, JavaScript Alerts"
    })
    void testSpecificPages(String browserName, String path, String expectedText) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = "firefox".equals(browserName) 
                ? playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(true))
                : playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            page.navigate("https://the-internet.herokuapp.com" + path);
            page.waitForLoadState();
            
            // Проверяем что страница содержит ожидаемый текст
            String pageContent = page.textContent("body");
            assertTrue(pageContent.contains(expectedText),
                      "Page should contain: " + expectedText);
            
            context.close();
            browser.close();
        }
    }
}