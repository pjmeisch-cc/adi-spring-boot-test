package com.adidas.cucumber.example;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;


@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"html:target/cucumber"}, format={"json:target/cucumber.json"})
public class RunCucumberTest {}
