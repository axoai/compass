Feature: Read value

  Scenario: Get value
    Given the following keys:
      | id         | schema.type | description |
      | "tax-rate" | "number"    | ".."        |
    And the following values for key tax-rate:
      | value |
      | 0.19  |
    When "GET /keys/tax-rate/value?country=DE" returns "200 OK" with:
      | value |
      | 0.19  |

  Scenario: Get value with dimension
    Given the following dimensions:
      | id        | schema.type | relation | description          |
      | "country" | "string"    | "="      | "ISO 3166-1 alpha-2" |
    And the following keys:
      | id         | schema.type | description |
      | "tax-rate" | "number"    | ".."        |
    And the following values for key tax-rate:
      | dimensions.country | value |
      | "CH"               | 0.08  |
      | "DE"               | 0.19  |
    When "GET /keys/tax-rate/value?country=DE" returns "200 OK" with:
      | value |
      | 0.19  |

  Scenario: Get value with dimension present
    Given the following dimensions:
      | id        | schema.type | relation | description          |
      | "country" | "string"    | "="      | "ISO 3166-1 alpha-2" |
      | "before"  | "string"    | "<"      | "ISO 8601"           |
    And the following keys:
      | id         | schema.type | description |
      | "tax-rate" | "number"    | ".."        |
    And the following values for key tax-rate:
      | dimensions.before      | value |
      | "2007-01-01T00:00:00Z" | 0.16  |
    And the following values for key tax-rate:
      | dimensions.country | value |
      | "DE"               | 0.19  |
    Then "GET /keys/tax-rate/value?before" returns "200 OK" with:
      | value |
      | 0.16  |
    Then "GET /keys/tax-rate/value?country" returns "200 OK" with:
      | value |
      | 0.19  |

  Scenario: Canonical value URL
    Given the following dimensions:
      | id        | schema.type | relation | description          |
      | "country" | "string"    | "="      | "ISO 3166-1 alpha-2" |
      | "before"  | "string"    | "<"      | "ISO 8601"           |
    And the following keys:
      | id         | schema.type | description |
      | "tax-rate" | "number"    | ".."        |
    And the following values for key tax-rate:
      | dimensions.country | dimensions.before      | value |
      | "DE"               | "2007-01-01T00:00:00Z" | 0.16  |
      | "DE"               | "2018-01-01T00:00:00Z" | 0.19  |
    When "GET /keys/tax-rate/value?country=DE&before=2017-06-10T14:03:21Z" returns "200 OK" with headers:
      | Content-Location                                                                 |
      | http://localhost:8080/keys/tax-rate/value?before=2018-01-01T00:00:00Z&country=DE |

  Scenario: Get all values
    Given the following keys:
      | id    | schema.type | relation | description |
      | "foo" | "string"    | "="      | "."         |
      | "bar" | "string"    | "="      | "."         |
      | "baz" | "string"    | "="      | "."         |
    And the following values for key foo:
      | value |
      | "foo" |
    And the following values for key bar:
      | value |
      | "bar" |
    And the following values for key baz:
      | value |
      | "baz" |
    When "GET /values" returns "200 OK" with a list of entries/foo/values:
      | value |
      | "foo" |
    And "GET /values" returns "200 OK" with a list of entries/bar/values:
      | value |
      | "bar" |
    And "GET /values" returns "200 OK" with a list of entries/baz/values:
      | value |
      | "baz" |

  Scenario: Get all values by key pattern
    Given the following keys:
      | id    | schema.type | relation | description |
      | "foo" | "string"    | "="      | "."         |
      | "bar" | "string"    | "="      | "."         |
      | "baz" | "string"    | "="      | "."         |
    And the following values for key foo:
      | value |
      | "foo" |
    And the following values for key bar:
      | value |
      | "bar" |
    And the following values for key baz:
      | value |
      | "baz" |
    When "GET /values?q=a" returns "200 OK" with an empty list of entries/foo/values
    And "GET /values?q=a" returns "200 OK" with a list of entries/bar/values:
      | value |
      | "bar" |
    And "GET /values?q=a" returns "200 OK" with a list of entries/baz/values:
      | value |
      | "baz" |
