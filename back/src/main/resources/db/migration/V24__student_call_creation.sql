CREATE TABLE student_call (
  id         BIGINT PRIMARY KEY NOT NULL,
  student_id BIGINT,
  date       DATE               NOT NULL DEFAULT current_date,
  name       VARCHAR(50)        NOT NULL DEFAULT '',
  method     INTEGER            NOT NULL DEFAULT 0,
  comment    TEXT               NOT NULL DEFAULT '',
  CONSTRAINT fk_student_call_student FOREIGN KEY (student_id) REFERENCES student (id)
);

CREATE SEQUENCE student_call_seq;