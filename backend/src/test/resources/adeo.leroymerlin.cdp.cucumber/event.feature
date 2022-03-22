Feature: Event functionalities
  This feature contains a list of functionalities related to event

  Scenario: sanity check of cucumber

    Given this list of events :
      | Title             |  NbStars | Comment     |id|
      | Jazz in Lille     |  5       |             |1 |
      | Hard rock folies  |  1       | So noisy !  |2 |
      | Old melodies      |          | For aged    |3 |
      | Pop 80th          |  3       | [blank]     |4 |


    Then the events count is 4

    Then  this last list of events :
      | Title            | NbStars | Comment    |
      | Jazz in Lille    | 5       |            |
      | Hard rock folies | 1       | So noisy ! |
      | Old melodies     |         | For aged   |
      | Pop 80th         | 3       | [blank]    |

