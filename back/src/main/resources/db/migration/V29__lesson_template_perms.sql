INSERT INTO apppermission (id) VALUES (18);
/*read lesson template*/
INSERT INTO apppermission (id) VALUES (19);
/*modify lesson template*/

INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('director', 18);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('admin', 18);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('manager', 18);

INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('director', 19);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('admin', 19);