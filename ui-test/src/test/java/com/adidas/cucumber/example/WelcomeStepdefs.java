package com.adidas.cucumber.example;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;
import org.openqa.selenium.WebDriver;

public class WelcomeStepdefs {
    private final WebDriver webDriver;
    private WelcomePage welcomePage;

    public WelcomeStepdefs() {
        this.webDriver = SharedDriver.getInstance();
        this.welcomePage = new WelcomePage(webDriver);
    }

    @Given("^I am on the welcome page$")
    public void i_am_on_the_welcome_page() {
        welcomePage.open();
    }

    @When("^I follow the products list link$")
    public void i_click_the_products_list_link() {
        welcomePage.clickProductsListLink();
    }

    @Then("^I should see \"(.+)\"$")
    public void i_should_see(String text) {
        ProductsPage productsPage = new ProductsPage(webDriver);
        productsPage.findVisibleText(text);
    }

}
