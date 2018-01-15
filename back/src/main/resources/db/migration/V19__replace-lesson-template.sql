delete from studentslot;
delete from lessonslot;
delete from templatelessonslot;

select setval('template_lesson_slot_seq', 1);

insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 3, 690, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 5, 960, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1035, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1110, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 6, 615, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 690, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 6, 885, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 960, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 6, 1035, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 0, 615, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 690, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 885, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 0, 960, 0, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 1035, 0, 1);

insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 4, 690, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 5, 960, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1035, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1110, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 615, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 690, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 6, 885, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 960, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 6, 1035, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 615, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 690, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 885, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 0, 960, 1, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 1035, 1, 1);

insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 5, 960, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1035, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1110, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 0, 885, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 960, 2, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 0, 1035, 2, 1);

insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 4, 690, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 5, 960, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1035, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1110, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 615, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 690, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 885, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 6, 960, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 1035, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 615, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 690, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 0, 885, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 960, 3, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 0, 1035, 3, 1);

insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 5, 690, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 5, 960, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1035, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1110, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 615, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 690, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 6, 885, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 960, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 1035, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 615, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 690, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 885, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 2, 0, 960, 4, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 1035, 4, 1);

insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 3, 960, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1035, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 3, 1110, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 4, 960, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1035, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 4, 1110, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 5, 960, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1035, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 5, 1110, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 615, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 6, 690, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 885, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 960, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 6, 1035, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 615, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 0, 0, 690, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 885, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 960, 5, 1);
insert into templatelessonslot (id, agegroup, day, frommins, subject, lesson_template_id) VALUES (nextval('template_lesson_slot_seq'), 1, 0, 1035, 5, 1);