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
        1504259907033001,
       '2016-06-24 20:45:01.000'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (2, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       1504259907033002,
       '2016-06-24 20:45:02.000'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (3, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
        1504259907033003,
       '2016-06-24 20:45:03.000'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (4, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
        1504259907033004,
        CURRENT_DATE
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (5, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
        1504259907033005,
       '2016-06-24 20:45:05.000'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (6, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
        1504259907033006,
       '2016-06-24 20:45:06.000'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (7, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
        1504259907033007,
       '2016-06-24 20:45:07.000'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (8, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
        1504259907033008,
       '2016-06-24 20:45:08.000'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (9, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
        1504259907033009,
       '2016-06-24 20:45:09.000'
);

INSERT INTO case_data (id, case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES (10, 'FT_MasterCaseType', 'BEFTA_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
        1504259907033010,
       '2016-06-24 20:45:10.000'
);

INSERT INTO case_link (case_id, case_type_id, linked_case_id) VALUES
  (10, 'FT_MasterCaseType', 1),
  (9, 'FT_MasterCaseType', 2),
  (9, 'FT_MasterCaseType', 3),
  (8, 'FT_MasterCaseType', 1),
  (10, 'FT_MasterCaseType', 2),
  (7, 'FT_MasterCaseType', 2),
  (7, 'FT_MasterCaseType', 3),
  (7, 'FT_MasterCaseType', 4),
  (7, 'FT_MasterCaseType', 6),
  (8, 'FT_MasterCaseType', 5)
