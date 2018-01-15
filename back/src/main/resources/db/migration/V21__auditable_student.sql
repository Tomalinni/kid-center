ALTER TABLE student
  ADD COLUMN created_date TIMESTAMP NOT NULL DEFAULT current_timestamp;
ALTER TABLE student
  ADD COLUMN created_by TEXT NOT NULL DEFAULT '';
ALTER TABLE student
  ADD COLUMN modified_date TIMESTAMP NOT NULL DEFAULT current_timestamp;
ALTER TABLE student
  ADD COLUMN modified_by TEXT NOT NULL DEFAULT '';