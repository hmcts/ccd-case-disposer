DELETE FROM case_event;
DELETE FROM case_link;
DELETE FROM case_data;

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES (1, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet",
         "SearchCriteria": {"classification":"PUBLIC",
               "value" : {
        "SearchParties" : {
        "classification" : "PUBLIC",
                    "value" : [
                      {
                        "value" : {
                          "Name" : "PUBLIC",
                          "EmailAddress" : "PUBLIC",
                          "AddressLine1" : "PUBLIC",
                          "PostCode" : "PUBLIC",
                          "DateOfBirth" : "PUBLIC",
                          "DateOfDeath" : "PUBLIC"
                        },
                        "id" : "[[ANYTHING_PRESENT]]"
                      }]
      }
    }
        }
        }',
        '{
          "PersonFirstName": "PUBLIC"
        }',
        1504259907351111,
        '2016-06-24 20:44:52.824');