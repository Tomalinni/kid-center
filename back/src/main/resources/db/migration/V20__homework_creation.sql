CREATE TABLE homework
(
    id BIGINT PRIMARY KEY NOT NULL,
    subject INTEGER NOT NULL,
    agegroup INTEGER NOT NULL,
    startdate DATE,
    enddate DATE
);

CREATE SEQUENCE homework_seq;

insert into apppermission (id) values (16); /*read homework*/
insert into apppermission (id) values (17); /*modify homework*/

insert into approle_apppermission (approle_id, permissions_id) values ('director', 16);
insert into approle_apppermission (approle_id, permissions_id) values ('admin', 16);
insert into approle_apppermission (approle_id, permissions_id) values ('relative', 16);
insert into approle_apppermission (approle_id, permissions_id) values ('manager', 16);

insert into approle_apppermission (approle_id, permissions_id) values ('director', 17);
insert into approle_apppermission (approle_id, permissions_id) values ('admin', 17);