ALTER TABLE student
  ADD COLUMN manager_id BIGINT;
ALTER TABLE student
  ADD CONSTRAINT fk_student_manager FOREIGN KEY (manager_id) REFERENCES teacher (id)
