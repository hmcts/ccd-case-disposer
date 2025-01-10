DELETE FROM case_data;

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (1, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
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
VALUES (2, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
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
VALUES (3, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       1504259907353527,
       NOW()
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (4, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       1504259907353526,
       NOW()
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (5, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       1504259907353525,
       NOW()
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (6, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       1504259907353524,
       NOW()
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (7, 'deletable_case_type', 'deletable_jurisdiction', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       1504259907353523,
       NOW()
);

