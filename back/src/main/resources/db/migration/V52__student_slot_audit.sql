ALTER TABLE studentslot
  ADD COLUMN created_date TIMESTAMP NOT NULL DEFAULT current_timestamp;
ALTER TABLE studentslot
  ADD COLUMN created_by TEXT NOT NULL DEFAULT 'system';
ALTER TABLE studentslot
  ADD COLUMN modified_date TIMESTAMP NOT NULL DEFAULT current_timestamp;
ALTER TABLE studentslot
  ADD COLUMN modified_by TEXT NOT NULL DEFAULT 'system';
UPDATE studentslot
SET created_date = '2017-08-01 00:00:00', modified_date = '2017-08-01 00:00:00'