ALTER TABLE student
  ADD COLUMN primary_relative TEXT NOT NULL DEFAULT '';
UPDATE student s
SET primary_relative = coalesce((SELECT name
                                 FROM studentrelative r
                                   JOIN student_studentrelative sr ON sr.relatives_id = r.id
                                 WHERE sr.student_id = s.id
                                 ORDER BY r.id ASC
                                 LIMIT 1), '');
ALTER TABLE student
  ADD COLUMN promoted_student_count INT NOT NULL DEFAULT 0;
