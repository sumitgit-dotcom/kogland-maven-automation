Feature: Kogland wishlist and guest login

  Background:
    Given I navigate to the Kogland home page

  @wishlist @smoke
Scenario: Add a Heart Health product to the wishlist and log in as a guest
    Given I navigate to the Kogland home page
    And I am logged in as a guest via cookie
    When I scroll to the "Heart Health" category on the home page
    And I click on the "Heart Health" category link
    And I add the "Ace Revelol 50/5 mg Tablet - Strip of 10" product to the wishlist
    Then the product should be in my wishlist
    And I click on the customer menu
    And I click on the "Email" login tab
    And I enter "rawatsumit14@gmail.com" into the guest email field
    And I enter "Sumitrawat@123" into the guest password field
    And I click the guest login submit button
    Then I should be logged in successfully