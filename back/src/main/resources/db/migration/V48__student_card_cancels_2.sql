ALTER TABLE card
  ADD COLUMN undue_cancels_limit INT NOT NULL DEFAULT 0;
ALTER TABLE card
  DROP COLUMN changeslimit;

ALTER TABLE studentcard
  ADD COLUMN undue_cancels_limit INT NOT NULL DEFAULT 0;
ALTER TABLE studentcard
  ADD COLUMN undue_cancels_available INT NOT NULL DEFAULT 0;
ALTER TABLE studentcard
  DROP COLUMN changeslimit;
ALTER TABLE studentcard
  DROP COLUMN changesavailable;

ALTER TABLE studentslot
  ADD COLUMN invalidated BOOL NOT NULL DEFAULT FALSE;