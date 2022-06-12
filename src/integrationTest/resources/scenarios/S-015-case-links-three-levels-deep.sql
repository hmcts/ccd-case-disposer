DELETE FROM case_event;
DELETE FROM case_link;
DELETE FROM case_data;

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (1, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       1504259907353529,
       '2016-06-24 20:44:52.824'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (2, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       1504259907353528,
       '2016-06-24 20:44:52.824'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (3, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       1504259907353527,
       '2016-06-24 20:44:52.824'
);

INSERT INTO case_link (case_id, case_type_id, linked_case_id)
VALUES (1, 'FT_MasterCaseType', 2);

INSERT INTO case_link (case_id, case_type_id, linked_case_id)
VALUES (2, 'FT_MasterCaseType', 3);
