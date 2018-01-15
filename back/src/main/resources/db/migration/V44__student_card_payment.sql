ALTER TABLE school
  ADD COLUMN external BOOL NOT NULL DEFAULT FALSE;
UPDATE school
SET external = TRUE
WHERE id = -1;

ALTER TABLE account
  ADD COLUMN businessId VARCHAR(32) NOT NULL DEFAULT '';
UPDATE account
SET businessId = 'M' || id;

ALTER TABLE studentcard
  ADD COLUMN payment_id BIGINT;
ALTER TABLE studentcard
  ADD CONSTRAINT fk_student_card_payment FOREIGN KEY (payment_id) REFERENCES payment (id);

CREATE TABLE preference
(
  id    VARCHAR(255) PRIMARY KEY NOT NULL,
  value TEXT
);

INSERT INTO apppermission (id) VALUES (24);
/*studentCardPaymentPrefModify*/

INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('director', 24);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('admin', 24);
