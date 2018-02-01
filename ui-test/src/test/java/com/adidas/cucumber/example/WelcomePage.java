package com.adidas.cucumber.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class WelcomePage extends BasePage {

    public WelcomePage(WebDriver webDriver) {
        super(webDriver);
    }

    public void open() {
        String url = System.getenv("SPRINGBOOT_SEED_URL");
        if (url == null) {
            String fallBackUrl = "http://springboot-seed:8080";
            System.out.println("Warning: You did not specify the target url of Springboot-Seed per " +
                "Environment variable  SPRINGBOOT_SEED_URL  - Using this instead as fallback: " + fallBackUrl);
            webDriver.get(fallBackUrl);
        } else {
            webDriver.get(url);
        }
    }

    public By productsListLink() {
        return By.linkText("Products List");
    }

    public void clickProductsListLink() {
        clickElementBy(productsListLink(), clickTimeoutSeconds);
    }

    public By loginLink() {
        return By.linkText("Log in using Adidas SSO (DEV)");
    }

    public void clickLoginLink() {
        clickElementBy(loginLink(), clickTimeoutSeconds);
    }
}
