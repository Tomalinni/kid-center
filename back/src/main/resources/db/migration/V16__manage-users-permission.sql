insert into apppermission (id) values (14); /*manage users*/
insert into approle_apppermission (approle_id, permissions_id) values ('director', 14);
insert into approle_apppermission (approle_id, permissions_id) values ('admin', 14);