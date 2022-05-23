
-- Delete existing data

Delete From auditentry;
Delete From documentmetadata;
Delete From storeddocument;

-- Stored Documents

INSERT INTO evidence.public.storeddocument(id, classification, createdby, deleted, harddeleted)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f608', 0, 'YH', false, false);

INSERT INTO evidence.public.storeddocument(id, classification, createdby, deleted, harddeleted)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f618', 1, 'RSR', false, false);

INSERT INTO evidence.public.storeddocument(id, classification, createdby, deleted, harddeleted)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f628', 2, 'MT', false, false);

INSERT INTO evidence.public.storeddocument(id, classification, createdby, deleted, harddeleted)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f638', 1, 'RSR', false, false);

INSERT INTO evidence.public.storeddocument(id, classification, createdby, deleted, harddeleted)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f648', 2, 'NJ', false, false);

INSERT INTO evidence.public.storeddocument(id, classification, createdby, deleted, harddeleted)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f658', 2, 'NJ', false, false);

-- Document Meta Data

INSERT INTO evidence.public.documentmetadata(documentmetadata_id, value, name)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f608', '234567890123406', 'case_id');

INSERT INTO evidence.public.documentmetadata(documentmetadata_id, value, name)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f618', '504259907353529', 'case_id');

INSERT INTO evidence.public.documentmetadata(documentmetadata_id, value, name)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f628', '504259907353529', 'case_id');

INSERT INTO evidence.public.documentmetadata(documentmetadata_id, value, name)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f638', '504259907353529', 'case_id');

INSERT INTO evidence.public.documentmetadata(documentmetadata_id, value, name)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f648', '504259907353528', 'case_id');

INSERT INTO evidence.public.documentmetadata(documentmetadata_id, value, name)
VALUES ('40e6215d-b5c6-4896-987c-f30f3678f658', '504259907353528', 'case_id');
