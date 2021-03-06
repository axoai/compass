Feature: Key creation

  Scenario: Creating a new key
    Given "GET /keys/example" responds "404 Not Found"
    When "PUT /keys/example" when requested with:
      | /id       | /schema/type | /description                 |
      | "example" | "string"     | "Lorem ipsum dolor sit amet" |
    Then "201 Created" was responded with:
      | /id       | /schema/type | /description                 |
      | "example" | "string"     | "Lorem ipsum dolor sit amet" |
    And "GET /keys/example" responds "200 OK" with:
      | /id       | /schema/type | /description                 |
      | "example" | "string"     | "Lorem ipsum dolor sit amet" |

  Scenario: Creating a new key succeeds when key doesn't exist
    Given "GET /keys/example" responds "404 Not Found"
    Then "PUT /keys/example" and "If-None-Match: *" responds "201 Created" when requested with:
      | /id       | /schema/type | /description                 |
      | "example" | "string"     | "Lorem ipsum dolor sit amet" |

  Scenario: Creating a new key fails when key already exists
    Given "PUT /keys/example" responds "201 Created" when requested with:
      | /id       | /schema/type | /description                 |
      | "example" | "string"     | "Lorem ipsum dolor sit amet" |
    Then "PUT /keys/example" and "If-None-Match: *" responds "412 Precondition Failed" when requested with:
      | /id       | /schema/type | /description                 |
      | "example" | "string"     | "Lorem ipsum dolor sit amet" |

  Scenario: Creating a new key failed due to schema violation
    When "PUT /keys/FOO" when requested with:
      | /schema/type | /description |
      | "any"        | false        |
    Then "400 Bad Request" was responded with an array at "/violations":
      | /message                                                                                                      |
      | "Instance failed to match all required schemas (matched only 1 out of 2)"                                     |
      | "[Path '/description'] Instance type (boolean) does not match any allowed primitive type (allowed: [string])" |
      | "[Path '/schema/type'] Instance failed to match at least one required schema among 2"                         |

  Scenario Outline: Creating a new key fails due to reserved keywords
    When "PUT /keys/<key>" when requested with:
      | /schema/type | /description                 |
      | "string"     | "Lorem ipsum dolor sit amet" |
    Then "400 Bad Request" was responded with an array at "/violations":
      | /message                                                                  |
      | "Instance failed to match all required schemas (matched only 1 out of 2)" |
    Examples:
      | key       |
      | cursor    |
      | embed     |
      | fields    |
      | filter    |
      | key       |
      | limit     |
      | offset    |
      | q         |
      | query     |
      | revisions |
      | sort      |
