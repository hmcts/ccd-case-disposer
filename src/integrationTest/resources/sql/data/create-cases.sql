DELETE FROM case_event;
DELETE FROM case_link;
DELETE FROM case_data;


INSERT INTO case_data (id, case_type_id, reference)
VALUES
(1, 'TestAddressBookCase', 1504735359825990),
(13, 'TestAddressBookCaseNoReadFieldAccess', 1504259907353651),
(14, 'TestAddressBookCase', 1504259907353598),
(2, 'TestCase', 1504735359905982),
(23, 'TestAccess', 1504259905365173),
(24, 'TestAddress', 1504259903535987),
(3, 'TestCase', 1504735359905982),
(32, 'TestAccess', 1504259905365173);

INSERT INTO case_link (case_id, case_type_id, linked_case_id)
VALUES
(1, 'TestAddressBookCaseNoReadFieldAccess', 13),
(1, 'TestAddressBookCase', 14),
(2, 'TestAccess', 23),
(2, 'TestAddress', 24),
(3, 'TestAccess', 32);
