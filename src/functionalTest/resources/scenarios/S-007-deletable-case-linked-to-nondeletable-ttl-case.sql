DELETE FROM case_link where case_id in (SELECT id FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages'));
DELETE FROM case_link where linked_case_id in (SELECT id FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages'));
DELETE FROM case_event where case_data_id in (SELECT id FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages'));
DELETE FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages');

INSERT INTO case_data (case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES ('DPR_FT_MasterCaseType', 'DISPOSER_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       504259907353529,
       '2016-06-24 20:44:52.824'
);

INSERT INTO case_data (case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES ('DPR_FT_MasterCaseType', 'DISPOSER_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       504259907353528,
       '2016-06-24 20:44:52.824'
);

INSERT INTO case_data (case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES ('DPR_FT_MultiplePages', 'DISPOSER_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       504259907353527,
       '2016-06-24 20:44:52.824'
);

INSERT INTO case_data (case_type_id, jurisdiction, state, security_classification, data, data_classification, reference, resolved_ttl)
VALUES ('DPR_FT_MasterCaseType', 'DISPOSER_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
       '{
         "PersonFirstName": "PUBLIC"
       }',
       504259907353526,
       '2121-06-24 20:44:52.824'
);

INSERT INTO case_link (case_id, case_type_id, linked_case_id)
VALUES (
  (select id from case_data where reference = 504259907353529),
  'DPR_FT_MasterCaseType',
  (select id from case_data where reference = 504259907353526));
