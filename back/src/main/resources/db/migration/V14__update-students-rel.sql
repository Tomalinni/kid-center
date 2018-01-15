alter table student_studentrelative drop constraint uk_fonur83j5l1j4sjfchpxu88hr;
alter table student_studentrelative add foreign key (relatives_id) references studentrelative (id);
alter table student_studentrelative add foreign key (student_id) references student (id);
alter table student drop mobile;
alter table studentrelative add column mobileconfirmed bool not null default false;

insert into apppermission (id) values (13);
insert into approle_apppermission (approle_id, permissions_id) values ('director', 13);
insert into approle_apppermission (approle_id, permissions_id) values ('admin', 13);

insert into approle(id) values ('manager');
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 0);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 1);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 2);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 3);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 4);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 5);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 6);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 7);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 8);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 9);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 10);

insert into appuser(id, name, pass) values ('testmanager','', 'f133b371abd7468b275930cf5fb8eb3371d32c18f619eb61adc933e1b138ea37d52f5cf30e90db2aeaeee657c4c28ca3b24bdacff92ae97665ef8dbca20e8e66');
insert into appuser_approle (appuser_id, roles_id) values ('testmanager', 'manager');