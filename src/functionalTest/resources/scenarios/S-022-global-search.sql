DELETE FROM case_link where case_id in (SELECT id FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages','DPR_FT_Conditionals'));
DELETE FROM case_link where linked_case_id in (SELECT id FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages','DPR_FT_Conditionals'));
DELETE FROM case_event where case_data_id in (SELECT id FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages','DPR_FT_Conditionals'));
DELETE FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages','DPR_FT_Conditionals');

INSERT INTO case_data (case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES ('DPR_FT_MasterCaseType', 'DISPOSER_MASTER', 'CaseCreated', 'PUBLIC',
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
        504259907351111,
        '2016-06-24 20:44:52.824');
