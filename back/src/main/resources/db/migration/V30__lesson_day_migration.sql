-- no lessons for monday and tuesday exist in template, using it here
UPDATE templatelessonslot
SET day = 2
WHERE day = 3;
UPDATE templatelessonslot
SET day = 3
WHERE day = 4;
UPDATE templatelessonslot
SET day = 4
WHERE day = 5;
UPDATE templatelessonslot
SET day = 5
WHERE day = 6;
UPDATE templatelessonslot
SET day = 6
WHERE day = 0;