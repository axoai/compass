Feature: Value history

  Background: Dimensions and key
    Given "PUT /dimensions/country" responds successfully when requested with:
      | /schema/type | /relation | /description         |
      | "string"     | "="       | "ISO 3166-1 alpha-2" |
    And "PUT /keys/tax-rate" responds successfully when requested with:
      | /schema/type | /description |
      | "number"     | ".."         |

  Scenario: Create/update/delete values and read revisions
    When "PUT /keys/tax-rate/values" responds successfully when requested with an array at "/values":
      | /dimensions/country | /value |
      | "CH"                | 0.08   |
      | "DE"                | 0.16   |
      | "AT"                | 0.2    |
    And "PUT /keys/tax-rate/values" responds successfully when requested with an array at "/values":
      | /dimensions/country | /value |
      | "AT"                | 0.2    |
      | "CH"                | 0.08   |
      | "DE"                | 0.19   |
    And "PUT /keys/tax-rate/values" responds successfully when requested with an array at "/values":
      | /dimensions/country | /value |
      | "AT"                | 0.2    |
      | "CH"                | 0.08   |
      | "FR"                | 0.2    |
    Then "GET /keys/tax-rate/value/revisions?country=DE" responds successfully with an array at "/revisions":
      | /dimensions/country | /revision/id | /revision/type | /revision/user | /revision/comment | /value |
      | "DE"                | 5            | "delete"       | "anonymous"    | ".."              | 0.19   |
      | "DE"                | 4            | "update"       | "anonymous"    | ".."              | 0.19   |
      | "DE"                | 3            | "create"       | "anonymous"    | ".."              | 0.16   |

  Scenario: Create/update/delete value and read revisions
    When "PUT /keys/tax-rate/value?country=DE" responds successfully when requested with:
      | /value |
      | 0.16   |
    When "PUT /keys/tax-rate/value?country=DE" responds successfully when requested with:
      | /value |
      | 0.19   |
    And "DELETE /keys/tax-rate/values?country=DE" responds successfully
    And "PUT /keys/tax-rate/value?country=DE" responds successfully when requested with:
      | /value |
      | 0.19   |
    And "DELETE /keys/tax-rate/values?country=DE" responds successfully
    Then "GET /keys/tax-rate/value/revisions?country=DE" responds successfully with an array at "/revisions":
      | /dimensions/country | /revision/id | /revision/type | /revision/user | /revision/comment | /value |
      | "DE"                | 7            | "delete"       | "anonymous"    | ".."              | 0.19   |
      | "DE"                | 6            | "create"       | "anonymous"    | ".."              | 0.19   |
      | "DE"                | 5            | "delete"       | "anonymous"    | ".."              | 0.19   |
      | "DE"                | 4            | "update"       | "anonymous"    | ".."              | 0.19   |
      | "DE"                | 3            | "create"       | "anonymous"    | ".."              | 0.16   |

  # TODO more values + from different keys to show that filtering works

  Scenario: Read deleted value
    When "PUT /keys/tax-rate/value?country=DE" responds successfully when requested with:
      | /value |
      | 0.19   |
    And "DELETE /keys/tax-rate/values?country=DE" responds successfully
    Then "GET /keys/tax-rate/value?country=DE" responds "410 Gone" with headers:
      | Location                                                       |
      | http://localhost:8080/keys/tax-rate/value/revisions?country=DE |

  Scenario: Read value revisions without dimensions
    When "PUT /keys/tax-rate/value" responds successfully when requested with:
      | /value |
      | 0.16   |
    And "PUT /keys/tax-rate/value" responds successfully when requested with:
      | /value |
      | 0.19   |
    And "DELETE /keys/tax-rate/values" responds successfully
    Then "GET /keys/tax-rate/value/revisions" responds successfully with an array at "/revisions":
      | /revision/id | /revision/type | /revision/user | /revision/comment | /value |
      | 5            | "delete"       | "anonymous"    | ".."              | 0.19   |
      | 4            | "update"       | "anonymous"    | ".."              | 0.19   |
      | 3            | "create"       | "anonymous"    | ".."              | 0.16   |

  Scenario: Read value revisions with multiple dimensions
    Given "PUT /dimensions/after" responds successfully when requested with:
      | /schema/type | /relation | /description |
      | "string"     | "="       | "ISO 8601"   |
    When "PUT /keys/tax-rate/value?country=DE&after=2007-01-01T00:00:00Z" responds successfully when requested with:
      | /value |
      | 0.19   |
    Then "GET /keys/tax-rate/value/revisions?country=DE&after=2007-01-01T00:00:00Z" responds successfully with an array at "/revisions":
      | /dimensions/country | /dimensions/after      | /revision/id | /revision/type | /revision/user | /revision/comment | /value |
      | "DE"                | "2007-01-01T00:00:00Z" | 4            | "create"       | "anonymous"    | ".."              | 0.19   |