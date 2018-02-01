package com.adidas.cucumber.example;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

public class SharedDriver extends EventFiringWebDriver {
    private static final WebDriver REAL_DRIVER;
    private static final Thread CLOSE_THREAD = new Thread() {
        @Override
        public void run() {
            REAL_DRIVER.close();
        }
    };
    private static final SharedDriver INSTANCE;

    private static URL getServiceURL() {
        try {
            return new URL(System.getenv("SELENIUM_URL"));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(CLOSE_THREAD);
        REAL_DRIVER = new RemoteWebDriver(getServiceURL(), DesiredCapabilities.chrome());
        INSTANCE = new SharedDriver();
    }

    public SharedDriver() {
        super(REAL_DRIVER);
    }

    public static SharedDriver getInstance() {
        return INSTANCE;
    }

    @Override
    public void close() {
        if (Thread.currentThread() != CLOSE_THREAD) {
            throw new UnsupportedOperationException("You shouldn't close this WebDriver. It's shared and will close when the JVM exits.");
        }
        super.close();
    }

    @Before
    public void deleteAllCookies() {
        manage().deleteAllCookies();
    }

    @After
    public void embedScreenshot(Scenario scenario) {
        try {
            byte[] screenshot = getScreenshotAs(OutputType.BYTES);
            scenario.embed(screenshot, "image/png");
        } catch (WebDriverException somePlatformsDontSupportScreenshots) {
            System.err.println(somePlatformsDontSupportScreenshots.getMessage());
        }
    }
}
