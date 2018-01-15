INSERT INTO apppermission (id) VALUES (20);
/*read student cards*/
INSERT INTO apppermission (id) VALUES (21);
/*modify student cards*/
INSERT INTO apppermission (id) VALUES (22);
/*read student calls*/
INSERT INTO apppermission (id) VALUES (23);
/*modify student calls*/

INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('director', 20);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('admin', 20);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('manager', 20);

INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('director', 21);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('admin', 21);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('manager', 21);

INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('director', 22);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('admin', 22);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('manager', 22);

INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('director', 23);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('admin', 23);
INSERT INTO approle_apppermission (approle_id, permissions_id) VALUES ('manager', 23);

