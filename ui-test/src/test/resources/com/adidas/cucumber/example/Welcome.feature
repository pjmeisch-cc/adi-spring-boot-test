Feature: SpringBoot-Seed site

  Scenario: Go to Products page
    Given I am on the welcome page
    When I follow the products list link
    Then I should see "Products"
