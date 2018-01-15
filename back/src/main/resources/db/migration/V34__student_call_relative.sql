ALTER TABLE student_call
  ADD COLUMN relative_id BIGINT;
ALTER TABLE student_call
  ADD CONSTRAINT fk_student_call_relative FOREIGN KEY (relative_id) REFERENCES studentrelative (id);