Feature: Event functionalities
  This feature contains a list of functionalities related to generic entities

  Scenario: test Given for generic entity with attributes named in column header

    Given this list of events :
#    Test when one column is id
#    Test less row persisted than in Cucumber table
      | Title            | NbStars | comment    |
      | Jazz in Lille    | 5       |            |
      | Hard rock folies | 1       | So noisy ! |
      | Old melodies     |         | For aged   |
      | Pop 80th         | 3       | [blank]    |

#      | Title            | NbStars | Comment    | Manager.firstName |
#      | Jazz in Lille    | 5       |            | Pierre            |
#      | Hard rock folies | 1       | So noisy ! | Pierre            |
#      | Old melodies     |         | For aged   | Jacques           |
#      | Pop 80th         | 3       | [blank]    | Jacques           |


    Given this list of bands :
      | Name           |
      | Crazy  trio    |
      | The black band |
      | Best friends   |

#    Given this list of testGenerics :
#      | Title          | thisIsTheId |
#      | Crazy  trio    | 1             |
#      | The black band | 2             |
#      | Best friends   | 3             |

    Then this last list of events :
#    test when one column is id
#    test with wrong column name
#    test with wrong value
#    test with [blank] versus null
      | Title            | NbStars | comment    |
      | Jazz in Lille    | 5       |            |
      | Hard rock folies | 1       | So noisy ! |
      | Old melodies     |         | For aged   |
      | Pop 80th         | 3       | [blank]    |