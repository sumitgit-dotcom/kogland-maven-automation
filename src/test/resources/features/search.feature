Feature: Kogland product search and cart

@search

  Scenario: Search for Sensodyne, filter in-stock, increase quantity, and add to cart
    Given I navigate to the Kogland home page
    When I click on the search box
    And I type "sensodyne" into the search box
    And I click on the "View all" link
    And I click on the "In stock" filter
    And I click on the product "Sensodyne Complete Protect+ Mouthwash - 250 ml"
    And I scroll to the quantity section on the product detail page
    And I increase the product quantity by 3
    And I click on the add to cart button
    Then the product should be added to the cart successfully