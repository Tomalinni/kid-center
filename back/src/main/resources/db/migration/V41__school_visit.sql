CREATE TABLE school_visit
(
  id         BIGINT PRIMARY KEY NOT NULL,
  student_id BIGINT,
  date       DATE               NOT NULL DEFAULT current_date,
  CONSTRAINT fk_school_visit_student FOREIGN KEY (student_id) REFERENCES student (id)
);

CREATE SEQUENCE school_visit_seq;