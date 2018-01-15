UPDATE studentslot
SET repeatsleft = 1
WHERE repeatsleft ISNULL;

ALTER TABLE studentslot
  ALTER COLUMN repeatsleft SET NOT NULL;
ALTER TABLE studentslot
  ALTER COLUMN repeatsleft SET DEFAULT 1;
