DELETE FROM case_event;
DELETE FROM case_link;
DELETE FROM case_data;


INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (5, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       5,
       '2016-06-24 20:44:52.824'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (6, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       6,
       '2016-06-24 20:44:52.824'
);

INSERT INTO case_event (id, event_id, event_name, case_data_id,user_id,case_type_id,case_type_version,state_id,data,
user_first_name,user_last_name,security_classification)
VALUES (5,'CREATED','CREATED', 5,5,'deletable_case_type',5,'CaseCreated',
    '{
      "PersonFirstName": "Janet"
    }',
    'Janet',
    'Doe',
    'PUBLIC'
 );

INSERT INTO case_event (id, event_id, event_name, case_data_id,user_id,case_type_id,case_type_version,state_id,data,
user_first_name,user_last_name,security_classification)
VALUES (6,'CREATED','CREATED', 6,6,'deletable_case_type',6,'CaseCreated',
  '{
      "PersonFirstName": "Janet"
   }',
  'Janet',
  'Doe',
  'PUBLIC'
);
