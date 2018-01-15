CREATE TABLE promotionsource
(
  id          BIGINT PRIMARY KEY NOT NULL,
  name        VARCHAR(50)        NOT NULL DEFAULT '',
  haspromoter BOOL               NOT NULL DEFAULT FALSE
);

CREATE SEQUENCE promotion_source_seq;

CREATE TABLE promotiondetail
(
  id                  BIGINT PRIMARY KEY NOT NULL,
  name                VARCHAR(50)        NOT NULL DEFAULT '',
  promotion_source_id BIGINT,
  CONSTRAINT fk_promotiondetail_source FOREIGN KEY (promotion_source_id) REFERENCES promotionsource (id)
);

CREATE SEQUENCE promotion_detail_seq;

ALTER TABLE student
  DROP COLUMN promotion_source;
ALTER TABLE student
  ADD COLUMN promotion_source BIGINT;
ALTER TABLE student
  ADD COLUMN promotion_detail BIGINT;
ALTER TABLE student
  ADD CONSTRAINT stud_promotion_source_fkey FOREIGN KEY (promotion_source) REFERENCES promotionsource (id);
ALTER TABLE student
  ADD CONSTRAINT stud_promotion_detail_fkey FOREIGN KEY (promotion_detail) REFERENCES promotiondetail (id);
