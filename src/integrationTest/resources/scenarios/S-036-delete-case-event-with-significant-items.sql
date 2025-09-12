DELETE FROM case_event;
DELETE FROM case_link;
DELETE FROM case_data;
DELETE FROM case_event_significant_items;

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES (1, 'TaskCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
        '{
          "PersonFirstName": "PUBLIC"
        }',
        1504259909035001,
        '2016-06-24');

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES (2, 'TaskCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
        '{
          "PersonFirstName": "PUBLIC"
        }',
        1504259909035002,
        '2016-06-24');


INSERT INTO case_event (id, event_id, event_name, case_data_id,user_id,case_type_id,case_type_version,state_id,data,
user_first_name,user_last_name,security_classification)
VALUES (1,'CREATED','CREATED', 1,1,'TaskCaseType',1,'CaseCreated',
    '{
      "PersonFirstName": "Janet"
    }',
    'Janet',
    'Doe',
    'PUBLIC'
 );

 INSERT INTO case_event (id, event_id, event_name, case_data_id,user_id,case_type_id,case_type_version,state_id,data,
user_first_name,user_last_name,security_classification)
VALUES (2,'CREATED','CREATED', 2,2,'TaskCaseType',1,'CaseCreated',
    '{
      "PersonFirstName": "Janet"
    }',
    'Janet',
    'Doe',
    'PUBLIC'
 );

 INSERT INTO case_event_significant_items (id, description, type, url,case_event_id)
VALUES (1,'TEST_MEDICINE','DOCUMENT','test url', 1);
