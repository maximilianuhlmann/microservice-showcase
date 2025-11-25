Feature: Billing Calculation
  As a billing system
  I want to calculate billing for customers
  So that I can generate accurate invoices

  Background:
    Given the billing API is available

  Scenario: Calculate billing for a customer
    Given customer "billing-customer-1" has usage events in period "2024-01-01"
    When I calculate billing for customer "billing-customer-1" and period "2024-01-01"
    Then the billing record should be created
    And the total amount should be "3.00"

  Scenario: Retrieve existing billing record
    Given customer "billing-customer-2" has usage events in period "2024-02-01"
    When I calculate billing for customer "billing-customer-2" and period "2024-02-01"
    And I retrieve the billing record for customer "billing-customer-2" and period "2024-02-01"
    Then the billing record should be found
    And the response status should be 200

  Scenario: Calculate billing with multiple usage events
    Given customer "billing-customer-3" has usage events in period "2024-03-01"
    When I calculate billing for customer "billing-customer-3" and period "2024-03-01"
    Then the billing record should be created
    And the total amount should be greater than "0"

