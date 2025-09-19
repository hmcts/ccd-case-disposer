DELETE FROM case_event_significant_items;
DELETE FROM case_event;
DELETE FROM case_link;
DELETE FROM case_data;



INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (12, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       12,
       '2016-06-24 20:44:52.824'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (13, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       13,
       '2016-06-24 20:44:52.824'
);


INSERT INTO case_event (id, event_id, event_name, case_data_id,user_id,case_type_id,case_type_version,state_id,data,
user_first_name,user_last_name,security_classification)
VALUES (12,'CREATED','CREATED', 12,12,'deletable_case_type',12,'CaseCreated',
    '{
      "PersonFirstName": "Janet"
    }',
    'Janet',
    'Doe',
    'PUBLIC'
 );

INSERT INTO case_event (id, event_id, event_name, case_data_id,user_id,case_type_id,case_type_version,state_id,data,
user_first_name,user_last_name,security_classification)
VALUES (13,'CREATED','CREATED', 13,13,'deletable_case_type',13,'CaseCreated',
    '{
      "PersonFirstName": "Janet"
    }',
    'Janet',
    'Doe',
    'PUBLIC'
 );

INSERT INTO case_event_significant_items (id, description, type, url,case_event_id)
VALUES (12,'TEST_MEDICINE1','DOCUMENT','test url1', 12);

INSERT INTO case_event_significant_items (id, description, type, url,case_event_id)
VALUES (13,'TEST_MEDICINE2','DOCUMENT','test url2', 12);

INSERT INTO case_event_significant_items (id, description, type, url,case_event_id)
VALUES (14,'TEST_MEDICINE3','DOCUMENT','test url3', 12);

INSERT INTO case_event_significant_items (id, description, type, url,case_event_id)
VALUES (15,'TEST_MEDICINE3','DOCUMENT','test url3', 13);
