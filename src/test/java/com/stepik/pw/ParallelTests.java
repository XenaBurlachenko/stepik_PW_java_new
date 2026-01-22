package com.stepik.pw;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParallelTests {
    private Playwright playwright;
    private Browser browser;

    @BeforeAll
    void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true));
    }

    @Test
    @DisplayName("Проверка заголовка страницы Login")
    void testLoginPage() {
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        
        try {
            page.navigate("https://the-internet.herokuapp.com/login");
            String title = page.title();
            System.out.println("Test 1 - Title: " + title + " | Thread: " + Thread.currentThread().getId());
            
            assertEquals("The Internet", title);
            
            String pageHeader = page.locator("h2").textContent();
            assertEquals("Login Page", pageHeader.trim());
            
        } finally {
            context.close();
        }
    }

    @Test
    @DisplayName("Проверка работы кнопки Add/Remove Elements")
    void testAddRemoveElements() {
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        
        try {
            page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");
            System.out.println("Test 2 - Started | Thread: " + Thread.currentThread().getId());
            
            int initialButtons = page.locator("button.added-manually").count();
            assertEquals(0, initialButtons);
            
            page.click("button:text('Add Element')");
            assertTrue(page.isVisible("button.added-manually"));
            
            page.click("button.added-manually");
            int finalButtons = page.locator("button.added-manually").count();
            assertEquals(0, finalButtons);
            
        } finally {
            context.close();
        }
    }

    @AfterAll
    void teardown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}