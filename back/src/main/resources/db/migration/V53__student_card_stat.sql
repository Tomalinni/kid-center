ALTER TABLE studentcard
  ADD COLUMN planned_lessons_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE studentcard
  ADD COLUMN spent_lessons_count INTEGER NOT NULL DEFAULT 0;
UPDATE studentcard sc
SET planned_lessons_count = (SELECT count(ss.id)
                             FROM studentslot ss
                             WHERE ss.card_id = sc.id AND ss.status = 0),
  spent_lessons_count     = (SELECT count(ss.id)
                             FROM studentslot ss
                             WHERE ss.card_id = sc.id AND ss.status IN (1, 2));