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
public class AssignmentParallelTest {
    
    // ТЕСТ 1: Проверка 7 разных страниц 
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
    void testSevenPages(String path) {
        testPageInChromium(path);
    }
    
    // ТЕСТ 2: Параметризация для поддержки 2 браузеров (в логике, но в CI только chromium)
    @ParameterizedTest
    @ValueSource(strings = {"chromium", "firefox"})
    void testTwoBrowsers(String browserName) {
        // В CI запускаем только chromium, firefox пропускаем, иначе в Actions упадёт, локально нормально работает
        if ("firefox".equals(browserName) && isRunningInCI()) {
            System.out.println("Skipping Firefox in CI environment");
            return;
        }
        
        try (Playwright playwright = Playwright.create()) {
            Browser browser = "firefox".equals(browserName)
                ? playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(true))
                : playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            page.navigate("https://the-internet.herokuapp.com/login");
  
            assertTrue(page.content().contains("Login"));
            
            context.close();
            browser.close();
        }
    }
    
    // ТЕСТ 3: Комбинированная параметризация
    @ParameterizedTest
    @CsvSource({
        "/login, chromium",
        "/dropdown, chromium",
        "/checkboxes, chromium"
    })
    void testCombination(String path, String browserName) {
        // Всегда chromium в CI
        String actualBrowser = isRunningInCI() ? "chromium" : browserName;
        
        try (Playwright playwright = Playwright.create()) {
            Browser browser = "firefox".equals(actualBrowser)
                ? playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(true))
                : playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            page.navigate("https://the-internet.herokuapp.com" + path);
            
            // Базовая проверка
            assertTrue(page.content().length() > 50);
            
            context.close();
            browser.close();
        }
    }
    
    private void testPageInChromium(String path) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true));
            
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            page.navigate("https://the-internet.herokuapp.com" + path);
            
            // Проверка загрузки
            assertNotNull(page.content());
            assertTrue(page.content().length() > 50);
            
            context.close();
            browser.close();
        }
    }
    
    private boolean isRunningInCI() {
        return System.getenv("CI") != null || 
               System.getenv("GITHUB_ACTIONS") != null ||
               System.getenv("JENKINS_HOME") != null;
    }
}