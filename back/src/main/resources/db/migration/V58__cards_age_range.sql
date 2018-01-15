ALTER TABLE card
  RENAME name TO agerange;
UPDATE card
SET agerange = 'r2_7';
ALTER TABLE studentcard
  RENAME name TO agerange;
UPDATE studentcard
SET agerange = 'r2_7';