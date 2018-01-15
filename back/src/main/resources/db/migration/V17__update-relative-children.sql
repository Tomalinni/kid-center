insert into apppermission (id) values (15); /*has children*/

insert into approle(id) values ('relative');
insert into approle_apppermission (approle_id, permissions_id) values ('relative', 0);
insert into approle_apppermission (approle_id, permissions_id) values ('relative', 15);

insert into appuser(id, name, pass) values ('testrelative','', 'f133b371abd7468b275930cf5fb8eb3371d32c18f619eb61adc933e1b138ea37d52f5cf30e90db2aeaeee657c4c28ca3b24bdacff92ae97665ef8dbca20e8e66');
insert into appuser_approle (appuser_id, roles_id) values ('testrelative', 'relative');