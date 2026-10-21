Feature: Kogland All Medicines flow

  Background:
    Given I open the Kogland home page

  @allMedicines
  Scenario: Filter Fast&Up products and add one to the wishlist
    When I go to the All Medicines category
    And I open the filter panel
    And I select the Fast&Up brand filter
    And I click the wishlist button for "Fast&Up Creatine + Electrolytes (Orange) - 15 Mix Sticks"
    Then the wishlist button should show as added