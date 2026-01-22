package com.stepik.pw;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public class ParallelTests {
    // ThreadLocal обеспечивает изоляцию между потоками
    private static ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    
    @BeforeAll
    void setup() {
        // Инициализация будет в каждом потоке отдельно
    }
    
    private Browser getBrowser() {
        // Ленивая инициализация браузера для каждого потока
        if (browserThreadLocal.get() == null) {
            playwrightThreadLocal.set(Playwright.create());
            browserThreadLocal.set(
                playwrightThreadLocal.get().chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true))
            );
        }
        return browserThreadLocal.get();
    }
    
    @AfterAll
    void teardown() {
        // Закрываем браузеры во всех потоках
        if (browserThreadLocal.get() != null) {
            browserThreadLocal.get().close();
        }
        if (playwrightThreadLocal.get() != null) {
            playwrightThreadLocal.get().close();
        }
    }

    @Test
    void testLoginPage() {
        Browser browser = getBrowser();
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        
        System.out.println("Test 1 started in thread: " + Thread.currentThread().getId());
        
        page.navigate("https://the-internet.herokuapp.com/login");
        
        // Проверяем заголовок h2 на странице
        String pageHeader = page.locator("h2").textContent();
        assertEquals("Login Page", pageHeader);
        
        context.close();
        
        System.out.println("Test 1 finished in thread: " + Thread.currentThread().getId());
    }

    @Test
    void testAddRemoveElements() {
        Browser browser = getBrowser();
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        
        System.out.println("Test 2 started in thread: " + Thread.currentThread().getId());
        
        page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");
        page.click("button:text('Add Element')");
        assertTrue(page.isVisible("button.added-manually"));
        
        context.close();
        
        System.out.println("Test 2 finished in thread: " + Thread.currentThread().getId());
    }
}