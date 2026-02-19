package demo;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CookiesDemoTest {

    private WebDriver driver;
    private JavascriptExecutor js;

    // We'll use a simple public site to demonstrate cookie operations
    // Important: navigate to the SAME DOMAIN before adding cookies, otherwise you'll get InvalidCookieDomainException.
    private static final String BASE_URL = "https://the-internet.herokuapp.com";

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        // options.addArguments("--headless=new"); // for CI if needed
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        js = (JavascriptExecutor) driver;

        // Navigate first so domain matches
        driver.get(BASE_URL + "/");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void createAndVerifyCookie_withPathAndSecurityFlags() {
        // Create a cookie with specific attributes:
        // - name/value
        // - path ("/") so it's sent for all paths on this domain
        // - expiry (1 week)
        // - Secure (sent only over HTTPS)
        // - HttpOnly (not visible to document.cookie)
        Cookie trainer = new Cookie.Builder("trainer", "Srujan")
                .path("/")
                // .domain("the-internet.herokuapp.com") // optional; Selenium infers current domain
                .expiresOn(Date.from(Instant.now().plus(Duration.ofDays(7))))
                .isSecure(true)
                .isHttpOnly(true)
                .build();

        driver.manage().addCookie(trainer);

        // Verify via Selenium cookie store
        Cookie fetched = driver.manage().getCookieNamed("trainer");
        Assert.assertNotNull(fetched, "Cookie should exist");
        Assert.assertEquals(fetched.getValue(), "Srujan");
        Assert.assertEquals(fetched.getPath(), "/");
        Assert.assertTrue(fetched.isSecure(), "Cookie should be Secure");
        Assert.assertTrue(fetched.isHttpOnly(), "Cookie should be HttpOnly");

        // Refresh so the cookie is sent with a new request
        driver.navigate().refresh();

        // HttpOnly cookie should NOT be visible via document.cookie
        String jsCookies = (String) js.executeScript("return document.cookie;");
        Assert.assertFalse(jsCookies.contains("trainer="),
                "HttpOnly cookie must not be readable by document.cookie");
    }

    @Test
    public void createUpdateDeleteCookie_visibleToJS_withPath() {
        // Create a cookie visible to JS (not HttpOnly), set path "/"
        Cookie visible = new Cookie.Builder("session_hint", "demo")
                .path("/")
                .isHttpOnly(false)
                .isSecure(false) // allow over http/https; site is https anyway
                .build();

        driver.manage().addCookie(visible);

        // Verify via Selenium
        Cookie got = driver.manage().getCookieNamed("session_hint");
        Assert.assertNotNull(got);
        Assert.assertEquals(got.getValue(), "demo");
        Assert.assertEquals(got.getPath(), "/");

        // Verify via JS
        String docCookie1 = (String) js.executeScript("return document.cookie;");
        Assert.assertTrue(docCookie1.contains("session_hint=demo"),
                "document.cookie should contain session_hint=demo");

        // Update cookie (overwrite same name) with tighter path to show path behavior
        // Note: If you change the path to something more specific, it will be sent only for that subpath.
        Cookie updated = new Cookie.Builder("session_hint", "updated_value")
                .path("/")              // keep as "/" for broad availability; change to "/abc" to limit scope
                .isHttpOnly(false)
                .build();
        driver.manage().addCookie(updated);

        // Verify updated value
        Cookie got2 = driver.manage().getCookieNamed("session_hint");
        Assert.assertEquals(got2.getValue(), "updated_value");

        String docCookie2 = (String) js.executeScript("return document.cookie;");
        Assert.assertTrue(docCookie2.contains("session_hint=updated_value"));

        // Delete single cookie
        driver.manage().deleteCookieNamed("session_hint");
        Assert.assertNull(driver.manage().getCookieNamed("session_hint"));

        String docCookie3 = (String) js.executeScript("return document.cookie;");
        Assert.assertFalse(docCookie3.contains("session_hint="));

        // Add two cookies, then delete all
        driver.manage().addCookie(new Cookie.Builder("a", "1").path("/").build());
        driver.manage().addCookie(new Cookie.Builder("b", "2").path("/").build());

        Set<Cookie> beforeDeleteAll = driver.manage().getCookies();
        Assert.assertTrue(beforeDeleteAll.size() >= 2, "At least two cookies should exist");

        driver.manage().deleteAllCookies();
        Assert.assertTrue(driver.manage().getCookies().isEmpty(), "All cookies should be deleted");
    }

    @Test
    public void createCookie_withSpecificPath_andDemonstrateScope() {
        // Create a cookie scoped only to /status_codes path
        Cookie pathScoped = new Cookie.Builder("scoped", "only-status")
                .path("/status_codes")
                .isHttpOnly(false)
                .isSecure(true)
                .build();

        driver.manage().addCookie(pathScoped);

        // Visiting a path OUTSIDE "/status_codes" may not expose it via document.cookie
        driver.get(BASE_URL + "/"); // homepage

        String docCookieRoot = (String) js.executeScript("return document.cookie;");
        // Because path is "/status_codes", many browsers won't show it on the root "/"
        Assert.assertFalse(docCookieRoot.contains("scoped=only-status"),
                "Cookie with path=/status_codes should not appear at root path");

        // Now go to a matching path where the cookie should be sent
        driver.get(BASE_URL + "/status_codes");
        String docCookieScoped = (String) js.executeScript("return document.cookie;");
        Assert.assertTrue(docCookieScoped.contains("scoped=only-status"),
                "Cookie should appear when path matches (/status_codes)");
    }
}