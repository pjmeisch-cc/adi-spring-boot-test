package com.adidas.cucumber.example;

import org.openqa.selenium.WebDriver;

public class ProductsPage extends BasePage {
    public ProductsPage(WebDriver webDriver) {
        super(webDriver);
    }

    public void open() {
        webDriver.get(System.getenv("SPRINGBOOT_SEED_URL")+"/products");
    }
}
