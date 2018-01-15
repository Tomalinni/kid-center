CREATE TABLE student_family (
  id BIGINT PRIMARY KEY NOT NULL
);

CREATE SEQUENCE student_family_seq;

ALTER TABLE student
  ADD COLUMN family_id BIGINT;
ALTER TABLE student
  ADD FOREIGN KEY (family_id) REFERENCES student_family (id);
ALTER TABLE studentrelative
  ADD COLUMN family_id BIGINT;
ALTER TABLE studentrelative
  ADD FOREIGN KEY (family_id) REFERENCES student_family (id);
