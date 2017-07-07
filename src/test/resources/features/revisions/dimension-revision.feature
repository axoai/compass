Feature: /dimensions/{id}/revision/{revision}

  Background: Updated dimension
    Given "PUT /dimensions/device" responds "201 Created" when requested with:
      | /schema/type | /relation | /description |
      | "string"     | "="       | ".."         |
    And "PUT /dimensions/device" responds "200 OK" when requested with:
      | /schema/type | /relation | /description               |
      | "string"     | "~"       | "Client Device Identifier" |
    And "DELETE /dimensions/device" responds "204 No Content"

  Scenario: Read revision
    Then "GET /dimensions/device/revisions/1" responds "200 OK" with:
      | /id      | /revision/id | /revision/type | /revision/user | /revision/comment | /schema/type | /relation | /description |
      | "device" | 1            | "create"       | "anonymous"    | ".."              | "string"     | "="       | ".."         |
    And "GET /dimensions/device/revisions/2" responds "200 OK" with:
      | /id      | /revision/id | /revision/type | /revision/user | /revision/comment | /schema/type | /relation | /description               |
      | "device" | 2            | "update"       | "anonymous"    | ".."              | "string"     | "~"       | "Client Device Identifier" |
    And "GET /dimensions/device/revisions/3" responds "200 OK" with:
      | /id      | /revision/id | /revision/type | /revision/user | /revision/comment | /schema/type | /relation | /description               |
      | "device" | 3            | "delete"       | "anonymous"    | ".."              | "string"     | "~"       | "Client Device Identifier" |