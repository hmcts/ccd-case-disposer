DELETE FROM case_link where case_id in (SELECT id FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages','DPR_FT_Conditionals','DPR_FT_HearingCaseType'));
DELETE FROM case_link where linked_case_id in (SELECT id FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages','DPR_FT_Conditionals','DPR_FT_HearingCaseType'));
DELETE FROM case_event_significant_items where case_event_id in (SELECT id FROM case_event where case_data_id in (SELECT id FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages','DPR_FT_Conditionals','DPR_FT_HearingCaseType')));
DELETE FROM case_event where case_data_id in (SELECT id FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages','DPR_FT_Conditionals','DPR_FT_HearingCaseType'));
DELETE FROM case_data where case_type_id in ('DPR_FT_MasterCaseType','DPR_FT_MultiplePages','DPR_FT_Conditionals','DPR_FT_HearingCaseType');

INSERT INTO case_data (case_type_id, jurisdiction, state, security_classification, data, data_classification,
                       reference, resolved_ttl)
VALUES ('DPR_FT_HearingCaseType', 'DISPOSER_MASTER', 'CaseCreated', 'PUBLIC',
        '{
          "PersonFirstName": "Janet"
        }',
        '{
          "PersonFirstName": "PUBLIC"
        }',
        1004259907359998,
        '2016-06-24 20:44:52.824');
