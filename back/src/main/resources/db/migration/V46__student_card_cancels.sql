ALTER TABLE card
  ADD COLUMN late_cancels_limit INT NOT NULL DEFAULT 0;
ALTER TABLE card
  ADD COLUMN last_moment_cancels_limit INT NOT NULL DEFAULT 0;

ALTER TABLE studentcard
  ADD COLUMN late_cancels_limit INT NOT NULL DEFAULT 0;
ALTER TABLE studentcard
  ADD COLUMN late_cancels_available INT NOT NULL DEFAULT 0;
ALTER TABLE studentcard
  ADD COLUMN last_moment_cancels_limit INT NOT NULL DEFAULT 0;
ALTER TABLE studentcard
  ADD COLUMN last_moment_cancels_available INT NOT NULL DEFAULT 0;

ALTER TABLE studentslot
  ADD COLUMN cancel_type INT;

