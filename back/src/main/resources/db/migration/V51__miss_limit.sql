ALTER TABLE card
  ADD COLUMN miss_limit INT NOT NULL DEFAULT 0;

ALTER TABLE studentcard
  ADD COLUMN miss_limit INT NOT NULL DEFAULT 0;
ALTER TABLE studentcard
  ADD COLUMN miss_available INT NOT NULL DEFAULT 0;