Feature: Kogland homepage link validation

  Scenario: Open homepage and print all clickable links
    Given I open the Kogland homepage
    When I collect all clickable links
    Then I print the total number of clickable links