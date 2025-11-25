Feature: Usage Events Management
  As a system administrator
  I want to manage usage events
  So that I can track customer usage for billing purposes

  Background:
    Given the API is available

  Scenario: Create a usage event
    Given a usage event with customer ID "customer-123", service type "api-calls", and quantity "10.5"
    When I create the usage event
    Then the usage event should be created successfully
    And the response status should be 201

  Scenario: Retrieve usage events by customer
    Given usage events exist for customer "customer-456"
    When I retrieve usage events for customer "customer-456"
    Then the response status should be 200
    And I should receive 2 usage events

  Scenario: Retrieve usage events by customer and service type
    Given usage events exist for customer "customer-789"
    When I retrieve usage events for customer "customer-789" and service type "api-calls"
    Then the response status should be 200
    And I should receive 1 usage events

  Scenario Outline: Create usage events with different service types
    Given a usage event with customer ID "<customerId>", service type "<serviceType>", and quantity "<quantity>"
    When I create the usage event
    Then the usage event should be created successfully

    Examples:
      | customerId   | serviceType | quantity |
      | customer-001 | api-calls   | 15.0     |
      | customer-002 | storage     | 20.5     |
      | customer-003 | compute     | 5.0      |

