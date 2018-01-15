ALTER TABLE student_call
  DROP COLUMN impression;
ALTER TABLE student
  ADD COLUMN impression INTEGER NOT NULL DEFAULT 0;