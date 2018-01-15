ALTER TABLE student
  ADD COLUMN promotion_source INT;
ALTER TABLE student
  ADD COLUMN promoter BIGINT;
ALTER TABLE student
  ADD COLUMN status INT NOT NULL DEFAULT 0;