DELETE
FROM case_event;
DELETE
FROM case_link;
DELETE
FROM case_data;

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES (1, 'HearingRecordings', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
        '{
          "PersonFirstName": "PUBLIC"
        }',
        1504259907445513,
        '2016-06-24 20:44:52.824');

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES (2, 'HearingRecordings', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
        '{
          "PersonFirstName": "PUBLIC"
        }',
        1504259907445514,
        '2016-06-24 20:44:52.824');
