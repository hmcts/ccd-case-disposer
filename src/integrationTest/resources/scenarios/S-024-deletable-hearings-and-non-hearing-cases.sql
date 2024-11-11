DELETE FROM case_event;
DELETE FROM case_link;
DELETE FROM case_data;

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES (1, 'HearingRecordings', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
        '{
          "PersonFirstName": "PUBLIC"
        }',
        4331434790352648,
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
        7344069216690734,
        '2016-06-24 20:44:52.824');

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES (3, 'FT_MultiplePages', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
        '{
          "PersonFirstName": "PUBLIC"
        }',
        6446787681450550,
        '2016-06-24 20:44:52.824');


