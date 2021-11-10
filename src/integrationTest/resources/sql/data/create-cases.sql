DELETE FROM case_event;
DELETE FROM case_link;
DELETE FROM case_data;


INSERT INTO case_data (id, case_type_id, reference)
VALUES
(1, 'TestAddressBookCase', '1504735359825990'),
(13, 'TestAddressBookCaseNoReadFieldAccess', '1504259907353651'),
(14, 'TestAddressBookCase', '1504259907353598');

INSERT INTO case_link (case_id, case_type_id, linked_case_id)
VALUES
(1, 'TestAddressBookCaseNoReadFieldAccess', 13),
(1, 'TestAddressBookCase', 14);
