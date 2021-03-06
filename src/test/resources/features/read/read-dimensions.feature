Feature: Read dimensions

  Scenario: Read dimensions
    Given "PUT /dimensions/{id}" (using /id) always responds "201 Created" when requested individually with:
      | /id        | /schema/type | /schema/format | /relation | /description |
      | "device"   | "string"     |                | "="       | ".."         |
      | "language" | "string"     | "bcp47"        | "^"       | ".."         |
      | "location" | "string"     | "geohash"      | "^"       | ".."         |
      | "before"   | "string"     | "date-time"    | "<="      | ".."         |
      | "after"    | "string"     | "date-time"    | ">="      | ".."         |
      | "email"    | "string"     | "email"        | "~"       | ".."         |
    When "GET /dimensions" responds "200 OK" with an array at "/dimensions":
      | /id        | /schema/type | /schema/format | /relation | /description |
      | "after"    | "string"     | "date-time"    | ">="      | ".."         |
      | "before"   | "string"     | "date-time"    | "<="      | ".."         |
      | "device"   | "string"     |                | "="       | ".."         |
      | "email"    | "string"     | "email"        | "~"       | ".."         |
      | "language" | "string"     | "bcp47"        | "^"       | ".."         |
      | "location" | "string"     | "geohash"      | "^"       | ".."         |

  Scenario: Read dimensions with limit of 0
    Given "PUT /dimensions/{id}" (using /id) always responds "201 Created" when requested individually with:
      | /id        | /schema/type | /schema/format | /relation | /description |
      | "device"   | "string"     |                | "="       | ".."         |
      | "language" | "string"     | "bcp47"        | "^"       | ".."         |
    Then "GET /dimensions?limit=0" responds "200 OK" with:
      | /dimensions | /next | /prev |
      | []          |       |       |
    And "GET /dimensions?limit=0&cursor=eyJkIjoiPiIsInAiOiJkZXZpY2UifQ" responds "200 OK" with:
      | /dimensions | /next | /prev |
      | []          |       |       |
    And "GET /dimensions?limit=0&cursor=eyJkIjoiPCIsInAiOiJsYW5ndWFnZSJ9" responds "200 OK" with:
      | /dimensions | /next | /prev |
      | []          |       |       |

  Scenario: Read dimensions with limit of 101
    Then "GET /dimensions?limit=101" responds "400 Bad Request" with an array at "/violations":
      | /message                                                                           |
      | "Numeric instance is greater than the required maximum (maximum: 100, found: 101)" |

  Scenario: Read empty dimensions
    Then "GET /dimensions" responds "200 OK" with an empty array at "/dimensions"
