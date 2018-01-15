UPDATE student
SET businessid = '';

ALTER TABLE student
  ALTER COLUMN businessid TYPE VARCHAR(4);
ALTER TABLE student
  ALTER COLUMN businessid SET NOT NULL;
ALTER TABLE student
  ALTER COLUMN businessid SET DEFAULT '';
ALTER TABLE student
  ADD COLUMN trialbusinessid VARCHAR(4) NOT NULL DEFAULT '';
ALTER SEQUENCE regular_id_seq RESTART;
ALTER SEQUENCE trial_id_seq RESTART;