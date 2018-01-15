ALTER TABLE studentcard
  ADD COLUMN source_transfer_card_id BIGINT;
ALTER TABLE studentcard
  ADD CONSTRAINT fk_student_card_transfer_card FOREIGN KEY (source_transfer_card_id) REFERENCES studentcard (id);
