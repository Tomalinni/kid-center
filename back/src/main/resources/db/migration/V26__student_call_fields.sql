ALTER TABLE student_call
  ADD COLUMN ressult INTEGER NOT NULL DEFAULT 0;
ALTER TABLE student_call
  ADD COLUMN impression INTEGER NOT NULL DEFAULT 0;
ALTER TABLE student_call
  DROP COLUMN name;
ALTER TABLE student_call
  ADD COLUMN employee_id BIGINT;
ALTER TABLE student_call
  ADD CONSTRAINT fk_student_call_employee FOREIGN KEY (employee_id) REFERENCES teacher (id)
