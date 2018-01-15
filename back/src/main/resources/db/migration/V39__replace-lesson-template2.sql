DELETE FROM studentslot;
DELETE FROM lessonslot;
DELETE FROM templatelessonslot;

SELECT setval('template_lesson_slot_seq', 1);

INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 2, 960, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1035, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1110, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 4, 690, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 5, 540, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 615, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 690, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 5, 885, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 960, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1035, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 6, 540, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 615, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 690, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 885, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 6, 960, 0, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 1035, 0, 1);

INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 2, 960, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1035, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1110, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 3, 690, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 540, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 615, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 690, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 885, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 5, 960, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1035, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 540, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 615, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 690, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 6, 885, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 960, 1, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 6, 1035, 1, 1);

INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 2, 960, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1035, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1110, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 5, 540, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 615, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 690, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 885, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 5, 960, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1035, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 6, 540, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 615, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 690, 2, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 1035, 2, 1);

INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 2, 960, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1035, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1110, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 5, 540, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 615, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 5, 690, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 885, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 960, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1035, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 6, 540, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 615, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 6, 690, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 885, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 960, 3, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 1035, 3, 1);

INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 2, 690, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 2, 960, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1035, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1110, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 5, 540, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 5, 615, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 690, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 5, 885, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 960, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 5, 1035, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 6, 540, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 6, 615, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 690, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 885, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 6, 960, 4, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 1035, 4, 1);

INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 2, 960, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1035, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 2, 1110, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 3, 690, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 540, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 615, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 690, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 5, 885, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 5, 960, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 5, 1035, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 540, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 615, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 690, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 885, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 2, 6, 960, 5, 1);
INSERT INTO templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id)
VALUES (nextval('template_lesson_slot_seq'), 1, 6, 1035, 5, 1);