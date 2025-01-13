DELETE FROM case_event;
DELETE FROM case_link;
DELETE FROM case_data;

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES (1, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
        '{
          "PersonFirstName": "PUBLIC"
        }',
        1504259907351162,
        '2016-06-24 20:45:00.000');

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES (2, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
        '{
          "PersonFirstName": "PUBLIC"
        }',
        1504259907351163,
        '2016-06-24 20:45:01.000');

INSERT INTO case_event (id, event_id, event_name, case_data_id,user_id,case_type_id,case_type_version,state_id,data,
user_first_name,user_last_name,security_classification)
VALUES (1,'CREATED','CREATED', 1,1,'FT_MasterCaseType',1,'CaseCreated',
    '{
      "PersonFirstName": "Janet"
    }',
    'Janet',
    'Doe',
    'PUBLIC'
 );

INSERT INTO case_event (id, event_id, event_name, case_data_id,user_id,case_type_id,case_type_version,state_id,data,
user_first_name,user_last_name,security_classification)
VALUES (2,'CREATED','CREATED', 1,2,'FT_MasterCaseType',1,'CaseCreated',
    '{
      "PersonFirstName": "Janet"
    }',
    'Janet',
    'Doe',
    'PUBLIC'
 );
