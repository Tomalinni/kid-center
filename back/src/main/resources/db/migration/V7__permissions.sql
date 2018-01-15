INSERT INTO public.apppermission (id) VALUES (0);
INSERT INTO public.apppermission (id) VALUES (1);
INSERT INTO public.apppermission (id) VALUES (2);
INSERT INTO public.apppermission (id) VALUES (3);
INSERT INTO public.apppermission (id) VALUES (4);
INSERT INTO public.apppermission (id) VALUES (5);
INSERT INTO public.apppermission (id) VALUES (6);
INSERT INTO public.apppermission (id) VALUES (7);
INSERT INTO public.apppermission (id) VALUES (8);
INSERT INTO public.apppermission (id) VALUES (9);
INSERT INTO public.apppermission (id) VALUES (10);
INSERT INTO public.apppermission (id) VALUES (11);
INSERT INTO public.apppermission (id) VALUES (12);
INSERT INTO public.apppermission (id) VALUES (13);

INSERT INTO public.approle (id) VALUES ('investor');
INSERT INTO public.approle (id) VALUES ('director');
INSERT INTO public.approle (id) VALUES ('admin');

INSERT INTO public.appuser (id, name, pass) VALUES ('yijun', '', '1c9838a7a78faee7a336a864cc3462684bcc8eda1f8e31c331535fa9eee1d8b4729af46fcf1406c925228c653dbbe4be9c4ed196f20a3e55b2f28c1bcdeb7d61');
INSERT INTO public.appuser (id, name, pass) VALUES ('sergey', '', 'b457b69932977cb24cefc4365dd52ed34b5db7fc9dd535cb4cbb0bbcfe54293223c948ba4969f3409c66f43c88bf793e6a98f15543e87fcded1d2cea9c635d48');
INSERT INTO public.appuser (id, name, pass) VALUES ('legohuman', '', '10ae0d6ee58d1413211409acdd41389d2ff41584e905bb1936ac490d809462fbafc95aca57ed6ccc3a36766dfc12c4b77f132d79baa0354a216e4ba0d4d4924d');
INSERT INTO public.appuser (id, name, pass) VALUES ('Michael_Z', '', '10ae0d6ee58d1413211409acdd41389d2ff41584e905bb1936ac490d809462fbafc95aca57ed6ccc3a36766dfc12c4b77f132d79baa0354a216e4ba0d4d4924d');

INSERT INTO public.appuser_approle (appuser_id, roles_id) VALUES ('yijun', 'investor');
INSERT INTO public.appuser_approle (appuser_id, roles_id) VALUES ('sergey', 'director');
INSERT INTO public.appuser_approle (appuser_id, roles_id) VALUES ('legohuman', 'admin');
INSERT INTO public.appuser_approle (appuser_id, roles_id) VALUES ('Michael_Z', 'admin');

INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('investor', 11);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 0);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 1);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 2);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 3);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 4);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 5);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 6);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 7);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 8);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 9);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 10);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 11);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 12);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 0);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 1);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 2);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 3);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 4);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 5);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 6);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 7);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 8);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 9);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 10);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 11);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 12);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('admin', 13);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('director', 13);
INSERT INTO public.approle_apppermission (approle_id, permissions_id) VALUES ('investor', 13);