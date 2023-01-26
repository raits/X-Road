#@Modifying
@ConfigurationInfo
Feature: Configuration Info APIs

  Scenario: View internal configuration
    When INTERNAL configuration parts exists
    And INTERNAL configuration source anchor info exists
    Then INTERNAL configuration source global download url exists

  Scenario: View external configuration
    When EXTERNAL configuration parts exists
    And EXTERNAL configuration source anchor info exists
    Then EXTERNAL configuration source global download url exists

  Scenario: Download internal configuration anchor
    When user downloads INTERNAL configuration source anchor
    Then it should return internal configuration source anchor file

  Scenario: Download external configuration anchor
    When user downloads EXTERNAL configuration source anchor
    Then it should return external configuration source anchor file

  Scenario: Download configuration part
    * User can download EXTERNAL configuration part SHARED-ID version 2
    * User can download INTERNAL configuration part SHARED-ID version 2
