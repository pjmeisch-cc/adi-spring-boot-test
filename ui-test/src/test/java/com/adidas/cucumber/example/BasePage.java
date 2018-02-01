package com.adidas.cucumber.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {
    final WebDriver webDriver;
    final int clickTimeoutSeconds = 3;
    final int findTextTimeoutSeconds = 3;

    public BasePage(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    void clickElementBy(By by, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(webDriver, timeoutSeconds);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));
        element.click();
    }

    public By findText(String text) {
        String xpath = "//*[contains(text(),'" + text + "')]";
        return By.xpath(xpath);
    }

    public WebElement findVisibleText(String text) {
        WebDriverWait wait = new WebDriverWait(webDriver, findTextTimeoutSeconds);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(findText(text)));
    }

    public String title() {
        return webDriver.getTitle();
    }
}
