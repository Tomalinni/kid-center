delete from public.student_studentrelative;
delete from public.studentrelative;
delete from public.student;
insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (1, 'AK1101', 'Winnie', '赵霖霖', '2011-06-25', 1, '13535559313', false);
insert into public.studentrelative (id, role, name, mobile) values (2, 'mother', null, '13535559313');
insert into public.student_studentrelative (student_id, relatives_id) values (1, 2);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (3, 'AK1102', 'Creamy', '夏婉晴', '2011-11-26', 1, '13826281343', false);
insert into public.studentrelative (id, role, name, mobile) values (4, 'father', '夏翔', '13826281343');
insert into public.student_studentrelative (student_id, relatives_id) values (3, 4);
insert into public.studentrelative (id, role, name, mobile) values (5, 'mother', null, '13682245668');
insert into public.student_studentrelative (student_id, relatives_id) values (3, 5);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (6, 'AK1103', 'Joe', '钟祖熙', '2007-11-19', 0, '13580527233', false);
insert into public.studentrelative (id, role, name, mobile) values (7, 'mother', '李亮', '13580527233');
insert into public.student_studentrelative (student_id, relatives_id) values (6, 7);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (8, 'AK1104', 'Zara', null, '2011-01-26', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (9, 'AK1105', 'Wendy', '陈思语', '2012-12-10', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (10, 'AK1106', 'Jolie', '朱力', '2011-09-09', 1, '13729886309', false);
insert into public.studentrelative (id, role, name, mobile) values (11, 'mother', 'Yiyi', '13729886309');
insert into public.student_studentrelative (student_id, relatives_id) values (10, 11);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (12, 'AK1107', null, '彭诗朗', '2012-08-15', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (13, 'father', '彭福盛', null);
insert into public.student_studentrelative (student_id, relatives_id) values (12, 13);
insert into public.studentrelative (id, role, name, mobile) values (14, 'mother', null, '18566553636');
insert into public.student_studentrelative (student_id, relatives_id) values (12, 14);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (15, 'AK1108', 'Isabella', '李明昕', '2011-12-08', 1, '13682228425', false);
insert into public.studentrelative (id, role, name, mobile) values (16, 'aunt', null, '13682228425');
insert into public.student_studentrelative (student_id, relatives_id) values (15, 16);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (17, 'AK1109', 'Jacob', '王稼皓', '2011-09-11', 0, '13924200605', false);
insert into public.studentrelative (id, role, name, mobile) values (18, 'father', '王春明', '13924200605');
insert into public.student_studentrelative (student_id, relatives_id) values (17, 18);
insert into public.studentrelative (id, role, name, mobile) values (19, 'mother', '谢菲', '13609016166');
insert into public.student_studentrelative (student_id, relatives_id) values (17, 19);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (20, 'AK1110', 'Dora', '曾顺鑫', '2011-09-13', 1, null, false);
insert into public.studentrelative (id, role, name, mobile) values (21, 'father', '曾兆旭', null);
insert into public.student_studentrelative (student_id, relatives_id) values (20, 21);
insert into public.studentrelative (id, role, name, mobile) values (22, 'mother', '高小兰', '13826183133');
insert into public.student_studentrelative (student_id, relatives_id) values (20, 22);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (23, 'AK1111', 'Bonnie', '祁上清', null, 1, '18620292339', false);
insert into public.studentrelative (id, role, name, mobile) values (24, 'father', '祁斌', '18620292339');
insert into public.student_studentrelative (student_id, relatives_id) values (23, 24);
insert into public.studentrelative (id, role, name, mobile) values (25, 'mother', '葉菲', '18620200606');
insert into public.student_studentrelative (student_id, relatives_id) values (23, 25);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (26, 'AK1112', 'Kitty', '李子瑶', '2013-06-02', 1, '13760699910', false);
insert into public.studentrelative (id, role, name, mobile) values (27, 'mother', '徐佩敏', '13760699910');
insert into public.student_studentrelative (student_id, relatives_id) values (26, 27);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (28, 'AK1113', null, '钟书陶', '2013-03-14', 0, '13434281128', false);
insert into public.studentrelative (id, role, name, mobile) values (29, 'mother', '夏琪', '13434281128');
insert into public.student_studentrelative (student_id, relatives_id) values (28, 29);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (30, 'AK1114', null, '车咏洛', '2012-02-26', 1, '15989266400', false);
insert into public.studentrelative (id, role, name, mobile) values (31, 'mother', '谭翠嘉', '15989266400');
insert into public.student_studentrelative (student_id, relatives_id) values (30, 31);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (32, 'AK1115', 'Candice', '姚玥', '2012-11-19', 1, '13570209442', false);
insert into public.studentrelative (id, role, name, mobile) values (33, 'mother', '燕丽娜', '13570209442');
insert into public.student_studentrelative (student_id, relatives_id) values (32, 33);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (34, 'AK1116', 'Ruby', '张一心', '2011-08-26', 1, '13760655809', false);
insert into public.studentrelative (id, role, name, mobile) values (35, 'mother', '冯', '13760655809');
insert into public.student_studentrelative (student_id, relatives_id) values (34, 35);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (36, 'AK1117', 'Anne', '梁雅雯', '2011-03-04', 1, '13924164021', false);
insert into public.studentrelative (id, role, name, mobile) values (37, 'mother', '苏奇', '13924164021');
insert into public.student_studentrelative (student_id, relatives_id) values (36, 37);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (38, 'AK1118', 'Aaliyha', null, '2013-03-24', 1, null, false);
insert into public.studentrelative (id, role, name, mobile) values (39, 'father', '葛生', null);
insert into public.student_studentrelative (student_id, relatives_id) values (38, 39);
insert into public.studentrelative (id, role, name, mobile) values (40, 'mother', null, '18600189992');
insert into public.student_studentrelative (student_id, relatives_id) values (38, 40);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (41, 'AK1119', 'William', '邓泽恩', '2011-02-28', 0, '13726803268', false);
insert into public.studentrelative (id, role, name, mobile) values (42, 'mother', '王彩琴', '13726803268');
insert into public.student_studentrelative (student_id, relatives_id) values (41, 42);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (43, 'AK1120', 'Amber', '黎恩希', '2013-04-22', 1, '13822235835', false);
insert into public.studentrelative (id, role, name, mobile) values (44, 'mother', '张颂宜', '13822235835');
insert into public.student_studentrelative (student_id, relatives_id) values (43, 44);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (45, 'AK1121', 'Panson', '潘广樾', '2012-12-08', 0, '18665611991', false);
insert into public.studentrelative (id, role, name, mobile) values (46, 'mother', '李清', '18665611991');
insert into public.student_studentrelative (student_id, relatives_id) values (45, 46);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (47, 'AK1122', 'Celeste', '田梓君', '2013-04-28', 1, '18620468761', false);
insert into public.studentrelative (id, role, name, mobile) values (48, 'father', null, '18620468761');
insert into public.student_studentrelative (student_id, relatives_id) values (47, 48);
insert into public.studentrelative (id, role, name, mobile) values (49, 'mother', null, '13480258684');
insert into public.student_studentrelative (student_id, relatives_id) values (47, 49);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (50, 'AK1123', null, '马依楠', '2013-01-30', 1, '13424493868', false);
insert into public.studentrelative (id, role, name, mobile) values (51, 'mother', null, '13424493868');
insert into public.student_studentrelative (student_id, relatives_id) values (50, 51);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (52, 'AK1124', 'Yoyo', '李心悠', '2013-04-23', 1, '18903002629', false);
insert into public.studentrelative (id, role, name, mobile) values (53, 'father', null, '18903002629');
insert into public.student_studentrelative (student_id, relatives_id) values (52, 53);
insert into public.studentrelative (id, role, name, mobile) values (54, 'mother', null, '18903002619');
insert into public.student_studentrelative (student_id, relatives_id) values (52, 54);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (55, 'AK1125', 'Sam', '靳昊燊', null, 0, '15800008534', false);
insert into public.studentrelative (id, role, name, mobile) values (56, 'father', '义宾', '15800008534');
insert into public.student_studentrelative (student_id, relatives_id) values (55, 56);
insert into public.studentrelative (id, role, name, mobile) values (57, 'mother', '梁玉婷', '13922701984');
insert into public.student_studentrelative (student_id, relatives_id) values (55, 57);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (58, 'AK1126', 'Sophia', '王诗绮', '2013-08-08', 1, '13602812579', false);
insert into public.studentrelative (id, role, name, mobile) values (59, 'father', '王健', '13602812579');
insert into public.student_studentrelative (student_id, relatives_id) values (58, 59);
insert into public.studentrelative (id, role, name, mobile) values (60, 'mother', '谢丹红', '13926170968');
insert into public.student_studentrelative (student_id, relatives_id) values (58, 60);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (61, 'AK1127', 'Dandan', '姚佳辰', '2013-08-27', 1, '13602812579', false);
insert into public.studentrelative (id, role, name, mobile) values (62, 'father', null, '13602812579');
insert into public.student_studentrelative (student_id, relatives_id) values (61, 62);
insert into public.studentrelative (id, role, name, mobile) values (63, 'mother', '陈惠娟', '13922201185');
insert into public.student_studentrelative (student_id, relatives_id) values (61, 63);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (64, 'AK1128', null, '邓彦彤', '2012-01-29', 1, '13922209239', false);
insert into public.studentrelative (id, role, name, mobile) values (65, 'father', null, '13922209239');
insert into public.student_studentrelative (student_id, relatives_id) values (64, 65);
insert into public.studentrelative (id, role, name, mobile) values (66, 'mother', '唐明', '13808818065');
insert into public.student_studentrelative (student_id, relatives_id) values (64, 66);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (67, 'AK1129', 'Tony', '吴俊恒', '2010-10-02', 0, '18620078620', false);
insert into public.studentrelative (id, role, name, mobile) values (68, 'mother', '苏静', '18620078620');
insert into public.student_studentrelative (student_id, relatives_id) values (67, 68);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (69, 'AK1130', 'Luci', '吴依婷', '2012-10-10', 1, '18620078620', false);
insert into public.studentrelative (id, role, name, mobile) values (70, 'mother', '苏静', '18620078620');
insert into public.student_studentrelative (student_id, relatives_id) values (69, 70);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (71, 'AK1131', 'Kim', '许乔睦', '2012-01-18', 0, '13763321463', false);
insert into public.studentrelative (id, role, name, mobile) values (72, 'father', '许晓心', '13763321463');
insert into public.student_studentrelative (student_id, relatives_id) values (71, 72);
insert into public.studentrelative (id, role, name, mobile) values (73, 'mother', '黄琼燕', '13822823668');
insert into public.student_studentrelative (student_id, relatives_id) values (71, 73);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (74, 'AK1132', null, '卢璟霖', '2012-07-06', 1, '13760776994', false);
insert into public.studentrelative (id, role, name, mobile) values (75, 'father', '卢俊韬', '13760776994');
insert into public.student_studentrelative (student_id, relatives_id) values (74, 75);
insert into public.studentrelative (id, role, name, mobile) values (76, 'mother', '李瑞丹', '13570277878');
insert into public.student_studentrelative (student_id, relatives_id) values (74, 76);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (77, 'AK1133', 'Kingsley', '吴启铭', '2012-03-23', 0, '18988840448', false);
insert into public.studentrelative (id, role, name, mobile) values (78, 'father', '吴炜强', '18988840448');
insert into public.student_studentrelative (student_id, relatives_id) values (77, 78);
insert into public.studentrelative (id, role, name, mobile) values (79, 'mother', '郑爱虹', '13824483131');
insert into public.student_studentrelative (student_id, relatives_id) values (77, 79);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (80, 'AK1134', 'Wilson', '吴耀森', '2013-09-22', 0, '18988840448', false);
insert into public.studentrelative (id, role, name, mobile) values (81, 'father', '吴炜强', '18988840448');
insert into public.student_studentrelative (student_id, relatives_id) values (80, 81);
insert into public.studentrelative (id, role, name, mobile) values (82, 'mother', '郑爱虹', '13824483131');
insert into public.student_studentrelative (student_id, relatives_id) values (80, 82);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (83, 'AK1135', 'Fiona', '郑舒贤', '2012-07-04', 1, '18688883966', false);
insert into public.studentrelative (id, role, name, mobile) values (84, 'father', '郑喆', '18688883966');
insert into public.student_studentrelative (student_id, relatives_id) values (83, 84);
insert into public.studentrelative (id, role, name, mobile) values (85, 'mother', '陈冬玲', '13829790900');
insert into public.student_studentrelative (student_id, relatives_id) values (83, 85);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (86, 'AK1136', 'Yumi', '方悦瑛', '2013-03-24', 1, '13682257265', false);
insert into public.studentrelative (id, role, name, mobile) values (87, 'mother', '方华', '13682257265');
insert into public.student_studentrelative (student_id, relatives_id) values (86, 87);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (88, 'AK1137', 'Hana', '陈盼', '2011-07-15', 1, '13560242952', false);
insert into public.studentrelative (id, role, name, mobile) values (89, 'father', null, '13560242952');
insert into public.student_studentrelative (student_id, relatives_id) values (88, 89);
insert into public.studentrelative (id, role, name, mobile) values (90, 'mother', null, '18922790620');
insert into public.student_studentrelative (student_id, relatives_id) values (88, 90);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (91, 'AK1138', 'Sara', '陈悦', '2012-09-21', 1, '13560242952', false);
insert into public.studentrelative (id, role, name, mobile) values (92, 'father', null, '13560242952');
insert into public.student_studentrelative (student_id, relatives_id) values (91, 92);
insert into public.studentrelative (id, role, name, mobile) values (93, 'mother', null, '18922790620');
insert into public.student_studentrelative (student_id, relatives_id) values (91, 93);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (94, 'AK1139', 'Sophia', '徐子墨', '2013-06-26', 1, '18680235467', false);
insert into public.studentrelative (id, role, name, mobile) values (95, 'father', '徐少彬', '18680235467');
insert into public.student_studentrelative (student_id, relatives_id) values (94, 95);
insert into public.studentrelative (id, role, name, mobile) values (96, 'mother', '冯', '13609739269');
insert into public.student_studentrelative (student_id, relatives_id) values (94, 96);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (97, 'AK1140', 'Ayaka', '井上绫香', '2013-07-11', 1, '13825025322', false);
insert into public.studentrelative (id, role, name, mobile) values (98, 'mother', null, '13825025322');
insert into public.student_studentrelative (student_id, relatives_id) values (97, 98);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (99, 'AK1141', 'Little', '梁艺桐', '2012-02-11', 1, '13527754594', false);
insert into public.studentrelative (id, role, name, mobile) values (100, 'father', '梁伟洪', '13527754594');
insert into public.student_studentrelative (student_id, relatives_id) values (99, 100);
insert into public.studentrelative (id, role, name, mobile) values (101, 'mother', '吴彩洋', '135-2775-4595');
insert into public.student_studentrelative (student_id, relatives_id) values (99, 101);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (102, 'AK1142', 'Lucky', '遇彤菲', '2010-07-27', 1, '13119507373', false);
insert into public.studentrelative (id, role, name, mobile) values (103, 'father', '姥姥', '13119507373');
insert into public.student_studentrelative (student_id, relatives_id) values (102, 103);
insert into public.studentrelative (id, role, name, mobile) values (104, 'mother', '闫伟华', '18665095777');
insert into public.student_studentrelative (student_id, relatives_id) values (102, 104);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (105, 'AK1143', 'Cindy', '谢欣予', '2011-09-05', 1, '13763331632', false);
insert into public.studentrelative (id, role, name, mobile) values (106, 'mother', '蒲瑜佳', '13763331632');
insert into public.student_studentrelative (student_id, relatives_id) values (105, 106);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (107, 'AK1144', 'Any', '陈栩丹', '2010-06-21', 1, '13288139888', false);
insert into public.studentrelative (id, role, name, mobile) values (108, 'mother', '陈燕旋', '13288139888');
insert into public.student_studentrelative (student_id, relatives_id) values (107, 108);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (109, 'AK1145', 'Kiki', '陈柳婷', '2012-02-02', 1, '13288139888', false);
insert into public.studentrelative (id, role, name, mobile) values (110, 'mother', '陈燕旋', '13288139888');
insert into public.student_studentrelative (student_id, relatives_id) values (109, 110);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (111, 'AK1146', 'Yigu', '陈奕燊', '2011-10-09', 0, '13501508832', false);
insert into public.studentrelative (id, role, name, mobile) values (112, 'father', '陈力衡', '13501508832');
insert into public.student_studentrelative (student_id, relatives_id) values (111, 112);
insert into public.studentrelative (id, role, name, mobile) values (113, 'mother', '邝虹', '13560300971');
insert into public.student_studentrelative (student_id, relatives_id) values (111, 113);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (114, 'AK1147', 'Hongkun', '黄鸿锟', '2012-03-01', 0, '15975632165', false);
insert into public.studentrelative (id, role, name, mobile) values (115, 'father', '黄锡嘉', '15975632165');
insert into public.student_studentrelative (student_id, relatives_id) values (114, 115);
insert into public.studentrelative (id, role, name, mobile) values (116, 'mother', '卢嘉丽', '13570568699');
insert into public.student_studentrelative (student_id, relatives_id) values (114, 116);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (117, 'AK1148', null, '余禧彤', '2013-01-02', 1, '13903069134', false);
insert into public.studentrelative (id, role, name, mobile) values (118, 'father', '余卓成', '13903069134');
insert into public.student_studentrelative (student_id, relatives_id) values (117, 118);
insert into public.studentrelative (id, role, name, mobile) values (119, 'mother', '陶燕', '13502418880');
insert into public.student_studentrelative (student_id, relatives_id) values (117, 119);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (120, 'AK1149', 'Miranda', '李佩颖', '2013-01-10', 1, '13928852798', false);
insert into public.studentrelative (id, role, name, mobile) values (121, 'father', '李龙山', '13928852798');
insert into public.student_studentrelative (student_id, relatives_id) values (120, 121);
insert into public.studentrelative (id, role, name, mobile) values (122, 'mother', '凌乐', '13922711868');
insert into public.student_studentrelative (student_id, relatives_id) values (120, 122);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (123, 'AK1150', 'Brayden', '吴予宽', '2015-01-06', 0, '13392688285', false);
insert into public.studentrelative (id, role, name, mobile) values (124, 'father', '高琼', '13392688285');
insert into public.student_studentrelative (student_id, relatives_id) values (123, 124);
insert into public.studentrelative (id, role, name, mobile) values (125, 'mother', '吴炯', '18903011715');
insert into public.student_studentrelative (student_id, relatives_id) values (123, 125);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (126, 'AK1151', null, '揭承熹', '2012-08-13', 0, '13922234448', false);
insert into public.studentrelative (id, role, name, mobile) values (127, 'father', null, '13922234448');
insert into public.student_studentrelative (student_id, relatives_id) values (126, 127);
insert into public.studentrelative (id, role, name, mobile) values (128, 'mother', '住红', '13560219085');
insert into public.student_studentrelative (student_id, relatives_id) values (126, 128);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (129, 'AK1152', 'Yaya', '牟晓雅', '2013-11-15', 1, '13925169193', false);
insert into public.studentrelative (id, role, name, mobile) values (130, 'father', '牟俊', '13925169193');
insert into public.student_studentrelative (student_id, relatives_id) values (129, 130);
insert into public.studentrelative (id, role, name, mobile) values (131, 'mother', '杨蕾', '13580300987');
insert into public.student_studentrelative (student_id, relatives_id) values (129, 131);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (132, 'AK1153', 'Alycia', '马佳妮', '2014-06-01', 1, '13436636682', false);
insert into public.studentrelative (id, role, name, mobile) values (133, 'father', 'Nylam Gauthier', '13436636682');
insert into public.student_studentrelative (student_id, relatives_id) values (132, 133);
insert into public.studentrelative (id, role, name, mobile) values (134, 'mother', '马晶晶', '13683351530');
insert into public.student_studentrelative (student_id, relatives_id) values (132, 134);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (135, 'AK1154', null, '罗一心', '2013-06-30', 1, '18924030433', false);
insert into public.studentrelative (id, role, name, mobile) values (136, 'father', '罗凯', '18924030433');
insert into public.student_studentrelative (student_id, relatives_id) values (135, 136);
insert into public.studentrelative (id, role, name, mobile) values (137, 'mother', '卢咏贞', '18127850033');
insert into public.student_studentrelative (student_id, relatives_id) values (135, 137);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (138, 'AK1155', 'Misha', null, null, 0, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (139, 'AK1156', null, '谢沛辰', '2014-08-05', 0, '13763331632', false);
insert into public.studentrelative (id, role, name, mobile) values (140, 'mother', '蒲瑜佳', '13763331632');
insert into public.student_studentrelative (student_id, relatives_id) values (139, 140);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (141, 'AK1157', 'Charlene', '李澄欣', '2012-09-07', 1, '13826188426', false);
insert into public.studentrelative (id, role, name, mobile) values (142, 'mother', '陈瑶', '13826188426');
insert into public.student_studentrelative (student_id, relatives_id) values (141, 142);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (143, 'AK1158', null, '武传哲', '2014-02-19', 0, '13316115080', false);
insert into public.studentrelative (id, role, name, mobile) values (144, 'father', '武家禄', '13316115080');
insert into public.student_studentrelative (student_id, relatives_id) values (143, 144);
insert into public.studentrelative (id, role, name, mobile) values (145, 'mother', '秦柳英', '13751705455');
insert into public.student_studentrelative (student_id, relatives_id) values (143, 145);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (146, 'AK1159', null, '黄诗涵', '2014-01-12', 1, '13570971234', false);
insert into public.studentrelative (id, role, name, mobile) values (147, 'mother', '王艳雪', '13570971234');
insert into public.student_studentrelative (student_id, relatives_id) values (146, 147);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (148, 'AK1160', 'Candy', '陈奕妃', '2012-01-24', 1, '13570958543', false);
insert into public.studentrelative (id, role, name, mobile) values (149, 'mother', '林灿', '13570958543');
insert into public.student_studentrelative (student_id, relatives_id) values (148, 149);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (150, 'AK1161', null, '黎恩彤', '2013-10-01', 1, '13828486443', false);
insert into public.studentrelative (id, role, name, mobile) values (151, 'father', '黎炜斌', '13828486443');
insert into public.student_studentrelative (student_id, relatives_id) values (150, 151);
insert into public.studentrelative (id, role, name, mobile) values (152, 'mother', '曾蒨雯', '13711446520');
insert into public.student_studentrelative (student_id, relatives_id) values (150, 152);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (153, 'AK1162', null, null, '2014-01-17', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (154, 'father', '陈东', null);
insert into public.student_studentrelative (student_id, relatives_id) values (153, 154);
insert into public.studentrelative (id, role, name, mobile) values (155, 'mother', '杨洁', '13828411522');
insert into public.student_studentrelative (student_id, relatives_id) values (153, 155);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (156, 'AK1163', 'Jiujiu', '胡嘉霖', '2013-09-09', 1, '13808818518', false);
insert into public.studentrelative (id, role, name, mobile) values (157, 'father', null, '13808818518');
insert into public.student_studentrelative (student_id, relatives_id) values (156, 157);
insert into public.studentrelative (id, role, name, mobile) values (158, 'mother', '刘青', '13710674203');
insert into public.student_studentrelative (student_id, relatives_id) values (156, 158);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (159, 'AK1164', null, '张诗童', '2011-01-09', 1, '13925187867', false);
insert into public.studentrelative (id, role, name, mobile) values (160, 'father', '张国瑞', '13925187867');
insert into public.student_studentrelative (student_id, relatives_id) values (159, 160);
insert into public.studentrelative (id, role, name, mobile) values (161, 'mother', '赵志美', '13926057877');
insert into public.student_studentrelative (student_id, relatives_id) values (159, 161);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (162, 'AK1165', 'Jacky', '廖浩扬', '2010-09-12', 0, '13826405099', false);
insert into public.studentrelative (id, role, name, mobile) values (163, 'father', '廖创祥', '13826405099');
insert into public.student_studentrelative (student_id, relatives_id) values (162, 163);
insert into public.studentrelative (id, role, name, mobile) values (164, 'mother', '顾晓婷', '13450271921');
insert into public.student_studentrelative (student_id, relatives_id) values (162, 164);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (165, 'AK1166', 'Jacob', '彭彦钧', '2011-12-19', 0, '13310188825', false);
insert into public.studentrelative (id, role, name, mobile) values (166, 'father', '彭志峰', '13310188825');
insert into public.student_studentrelative (student_id, relatives_id) values (165, 166);
insert into public.studentrelative (id, role, name, mobile) values (167, 'mother', '何景燕', '13316118331');
insert into public.student_studentrelative (student_id, relatives_id) values (165, 167);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (168, 'AK1167', null, '黄诗琪', '2013-10-23', 1, '13560132844', false);
insert into public.studentrelative (id, role, name, mobile) values (169, 'father', '黄超', '13560132844');
insert into public.student_studentrelative (student_id, relatives_id) values (168, 169);
insert into public.studentrelative (id, role, name, mobile) values (170, 'mother', '曾曼花', '13710538298');
insert into public.student_studentrelative (student_id, relatives_id) values (168, 170);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (171, 'AK1168', null, '王秋萤', '2012-12-31', 1, '15920420889', false);
insert into public.studentrelative (id, role, name, mobile) values (172, 'father', '王湃杰', '15920420889');
insert into public.student_studentrelative (student_id, relatives_id) values (171, 172);
insert into public.studentrelative (id, role, name, mobile) values (173, 'mother', '洪虹', '13580399979');
insert into public.student_studentrelative (student_id, relatives_id) values (171, 173);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (174, 'AK1169', 'Herman', '黄铭浩', '2012-10-15', 0, '13560351300', false);
insert into public.studentrelative (id, role, name, mobile) values (175, 'father', '电话：13580399979', '13560351300');
insert into public.student_studentrelative (student_id, relatives_id) values (174, 175);
insert into public.studentrelative (id, role, name, mobile) values (176, 'mother', '吴晓君', '13760690199');
insert into public.student_studentrelative (student_id, relatives_id) values (174, 176);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (177, 'AK1170', null, '夏子文', '2012-11-22', 1, '18802080871', false);
insert into public.studentrelative (id, role, name, mobile) values (178, 'father', null, '18802080871');
insert into public.student_studentrelative (student_id, relatives_id) values (177, 178);
insert into public.studentrelative (id, role, name, mobile) values (179, 'mother', '夏琴', '18927519364');
insert into public.student_studentrelative (student_id, relatives_id) values (177, 179);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (180, 'AK1171', 'Lisa', '张芮宁', '2012-09-25', 1, '13922187758', false);
insert into public.studentrelative (id, role, name, mobile) values (181, 'mother', null, '13922187758');
insert into public.student_studentrelative (student_id, relatives_id) values (180, 181);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (182, 'AK1172', 'Zhizhi', '张曦之', '2012-10-06', 1, '13828444492', false);
insert into public.studentrelative (id, role, name, mobile) values (183, 'father', '阿友', '13828444492');
insert into public.student_studentrelative (student_id, relatives_id) values (182, 183);
insert into public.studentrelative (id, role, name, mobile) values (184, 'mother', '娜娜', '13580557700');
insert into public.student_studentrelative (student_id, relatives_id) values (182, 184);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (185, 'AK1173', 'Daniel', '陈启元', '2013-07-07', 0, '13822287090', false);
insert into public.studentrelative (id, role, name, mobile) values (186, 'father', '陈叔盈', '13822287090');
insert into public.student_studentrelative (student_id, relatives_id) values (185, 186);
insert into public.studentrelative (id, role, name, mobile) values (187, 'mother', '招维璐', '13802908400');
insert into public.student_studentrelative (student_id, relatives_id) values (185, 187);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (188, 'AK1174', null, '苏志诚', '2013-10-10', 0, '13829771777', false);
insert into public.studentrelative (id, role, name, mobile) values (189, 'mother', '苏钰琪', '13829771777');
insert into public.student_studentrelative (student_id, relatives_id) values (188, 189);
insert into public.studentrelative (id, role, name, mobile) values (190, 'aunt', null, '18211477060');
insert into public.student_studentrelative (student_id, relatives_id) values (188, 190);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (191, 'AK1175', null, '曹乐颐', '2013-06-10', 1, '13602478888', false);
insert into public.studentrelative (id, role, name, mobile) values (192, 'father', '曹伟东', '13602478888');
insert into public.student_studentrelative (student_id, relatives_id) values (191, 192);
insert into public.studentrelative (id, role, name, mobile) values (193, 'mother', '肖蕾', '13822178922');
insert into public.student_studentrelative (student_id, relatives_id) values (191, 193);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (194, 'AK1176', 'Eugene', '邓如珺', '2012-08-04', 1, '13726803268', false);
insert into public.studentrelative (id, role, name, mobile) values (195, 'mother', '王彩琴', '13726803268');
insert into public.student_studentrelative (student_id, relatives_id) values (194, 195);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (196, 'AK1177', 'Thomas', '宋承熹', '2011-10-25', 0, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (197, 'AK1178', null, '宋承浩', '2013-01-30', 0, '13458049235', false);
insert into public.studentrelative (id, role, name, mobile) values (198, 'mother', null, '13458049235');
insert into public.student_studentrelative (student_id, relatives_id) values (197, 198);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (199, 'AK1179', 'Roy', '梁朗睿', '2013-01-02', 0, '13500027857', false);
insert into public.studentrelative (id, role, name, mobile) values (200, 'father', '梁哲铭', '13500027857');
insert into public.student_studentrelative (student_id, relatives_id) values (199, 200);
insert into public.studentrelative (id, role, name, mobile) values (201, 'mother', '韩莉', '13802521908');
insert into public.student_studentrelative (student_id, relatives_id) values (199, 201);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (202, 'AK1180', 'Amy', '郑涵', '2010-06-17', 1, '18680202888', false);
insert into public.studentrelative (id, role, name, mobile) values (203, 'father', '郑', '18680202888');
insert into public.student_studentrelative (student_id, relatives_id) values (202, 203);
insert into public.studentrelative (id, role, name, mobile) values (204, 'mother', '刘小奇', '13928725685');
insert into public.student_studentrelative (student_id, relatives_id) values (202, 204);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (205, 'AK1181', null, '郑媛', '2012-06-06', 1, '18680202888', false);
insert into public.studentrelative (id, role, name, mobile) values (206, 'father', '郑', '18680202888');
insert into public.student_studentrelative (student_id, relatives_id) values (205, 206);
insert into public.studentrelative (id, role, name, mobile) values (207, 'mother', '刘小奇', '13928725685');
insert into public.student_studentrelative (student_id, relatives_id) values (205, 207);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (208, 'AK1182', null, '王怡嘉', '2010-06-17', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (209, 'AK1183', 'Tina', '方煜婷', '2010-08-18', 1, '13580436688', false);
insert into public.studentrelative (id, role, name, mobile) values (210, 'father', '方生', '13580436688');
insert into public.student_studentrelative (student_id, relatives_id) values (209, 210);
insert into public.studentrelative (id, role, name, mobile) values (211, 'mother', '张亚秀', '13570546688');
insert into public.student_studentrelative (student_id, relatives_id) values (209, 211);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (212, 'AK1184', 'Darren', '曾家皓', '2013-12-18', 0, '13798119445', false);
insert into public.studentrelative (id, role, name, mobile) values (213, 'mother', '陈颖妍', '13798119445');
insert into public.student_studentrelative (student_id, relatives_id) values (212, 213);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (214, 'AK1185', 'Eason', null, '2013-12-18', 0, '13509290658', false);
insert into public.studentrelative (id, role, name, mobile) values (215, 'father', '爸爸', '13509290658');
insert into public.student_studentrelative (student_id, relatives_id) values (214, 215);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (216, 'AK1186', 'Aiden', null, '2013-12-18', 0, '13509290658', false);
insert into public.studentrelative (id, role, name, mobile) values (217, 'father', '爸爸', '13509290658');
insert into public.student_studentrelative (student_id, relatives_id) values (216, 217);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (218, 'AK1187', 'Maggie', '陈彦潼', '2010-11-01', 1, '18620022391', false);
insert into public.studentrelative (id, role, name, mobile) values (219, 'father', '陈思清', '18620022391');
insert into public.student_studentrelative (student_id, relatives_id) values (218, 219);
insert into public.studentrelative (id, role, name, mobile) values (220, 'mother', '李嘉雯', '18620022391');
insert into public.student_studentrelative (student_id, relatives_id) values (218, 220);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (221, 'AK1188', 'Hank', '许乔翰', '2014-10-17', 0, '13763321463', false);
insert into public.studentrelative (id, role, name, mobile) values (222, 'father', null, '13763321463');
insert into public.student_studentrelative (student_id, relatives_id) values (221, 222);
insert into public.studentrelative (id, role, name, mobile) values (223, 'mother', '黄琼燕', '13822823668');
insert into public.student_studentrelative (student_id, relatives_id) values (221, 223);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (224, 'AK1189', 'Sisina', '蒋粟乔雅', '2012-02-17', 1, '13925166767', false);
insert into public.studentrelative (id, role, name, mobile) values (225, 'mother', '宫娉婷', '13925166767');
insert into public.student_studentrelative (student_id, relatives_id) values (224, 225);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (226, 'AK1190', null, '王开心', '2013-06-06', 0, '18620066879', false);
insert into public.studentrelative (id, role, name, mobile) values (227, 'father', '王骞', '18620066879');
insert into public.student_studentrelative (student_id, relatives_id) values (226, 227);
insert into public.studentrelative (id, role, name, mobile) values (228, 'mother', '李丹', '18688862256');
insert into public.student_studentrelative (student_id, relatives_id) values (226, 228);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (229, 'AK1191', 'William', '黄彦凯', '2013-11-17', 0, '13902220968', false);
insert into public.studentrelative (id, role, name, mobile) values (230, 'father', '黄先生', '13902220968');
insert into public.student_studentrelative (student_id, relatives_id) values (229, 230);
insert into public.studentrelative (id, role, name, mobile) values (231, 'mother', '章小姐', '13928860720');
insert into public.student_studentrelative (student_id, relatives_id) values (229, 231);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (232, 'AK1192', 'Andy', '老大', null, 0, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (233, 'AK1193', 'Jessica', '陈玥霖', '2011-05-29', 1, '13809209308', false);
insert into public.studentrelative (id, role, name, mobile) values (234, 'mother', '谢茜', '13809209308');
insert into public.student_studentrelative (student_id, relatives_id) values (233, 234);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (235, 'AK1194', 'Samantha', '樊羽墨', '2007-08-04', 1, '18680582607', false);
insert into public.studentrelative (id, role, name, mobile) values (236, 'mother', null, '18680582607');
insert into public.student_studentrelative (student_id, relatives_id) values (235, 236);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (237, 'AK1195', 'Leo', '樊翰墨', '2011-08-08', 0, '18680582607', false);
insert into public.studentrelative (id, role, name, mobile) values (238, 'mother', null, '18680582607');
insert into public.student_studentrelative (student_id, relatives_id) values (237, 238);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (239, 'AK1196', 'Henry', '郭纯昊', '2012-04-01', 0, '13602813488', false);
insert into public.studentrelative (id, role, name, mobile) values (240, 'mother', null, '13602813488');
insert into public.student_studentrelative (student_id, relatives_id) values (239, 240);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (241, 'AK1197', 'Sam', '池启琛', '2013-07-31', 0, '13533338731', false);
insert into public.studentrelative (id, role, name, mobile) values (242, 'mother', '谭泳省', '13533338731');
insert into public.student_studentrelative (student_id, relatives_id) values (241, 242);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (243, 'AK1198', 'Cici', '陈嘉熙', '2013-02-16', 1, '13710611898', false);
insert into public.studentrelative (id, role, name, mobile) values (244, 'mother', null, '13710611898');
insert into public.student_studentrelative (student_id, relatives_id) values (243, 244);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (245, 'AK1199', 'Kaka', '张珈嘉', '2009-07-15', 1, '13600003453', false);
insert into public.studentrelative (id, role, name, mobile) values (246, 'mother', '王亦珺', '13600003453');
insert into public.student_studentrelative (student_id, relatives_id) values (245, 246);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (247, 'AK1200', 'Chole', '张珈旖', '2011-06-29', 1, '13600003453', false);
insert into public.studentrelative (id, role, name, mobile) values (248, 'mother', '王亦珺', '13600003453');
insert into public.student_studentrelative (student_id, relatives_id) values (247, 248);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (249, 'AK1201', 'Mark', '陈骏烨', '2012-03-29', 0, '18027197085', false);
insert into public.studentrelative (id, role, name, mobile) values (250, 'mother', '郑惠', '18027197085');
insert into public.student_studentrelative (student_id, relatives_id) values (249, 250);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (251, 'AK1202', 'Dora', '庞锦洋', '2012-04-11', 1, '18620811703', false);
insert into public.studentrelative (id, role, name, mobile) values (252, 'mother', '妈妈', '18620811703');
insert into public.student_studentrelative (student_id, relatives_id) values (251, 252);
insert into public.studentrelative (id, role, name, mobile) values (253, 'aunt', '外婆', '13610358277');
insert into public.student_studentrelative (student_id, relatives_id) values (251, 253);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (254, 'AK1203', 'Peppa', '梁淑瑜', '2011-08-11', 1, '13922113821', false);
insert into public.studentrelative (id, role, name, mobile) values (255, 'father', '李靖', '13922113821');
insert into public.student_studentrelative (student_id, relatives_id) values (254, 255);
insert into public.studentrelative (id, role, name, mobile) values (256, 'mother', null, '13922113821');
insert into public.student_studentrelative (student_id, relatives_id) values (254, 256);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (257, 'AK1204', 'Linlin', '杜林琳', '2013-05-06', 1, '13826029333', false);
insert into public.studentrelative (id, role, name, mobile) values (258, 'father', '杜正平', '13826029333');
insert into public.student_studentrelative (student_id, relatives_id) values (257, 258);
insert into public.studentrelative (id, role, name, mobile) values (259, 'mother', '林婉青', '13802429503');
insert into public.student_studentrelative (student_id, relatives_id) values (257, 259);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (260, 'AK1205', 'Ashley', '吕珈霖', '2012-11-02', 1, '18028607609', false);
insert into public.studentrelative (id, role, name, mobile) values (261, 'father', null, '18028607609');
insert into public.student_studentrelative (student_id, relatives_id) values (260, 261);
insert into public.studentrelative (id, role, name, mobile) values (262, 'mother', null, '13751770373');
insert into public.student_studentrelative (student_id, relatives_id) values (260, 262);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (263, 'AK1206', null, '沈嘉檀', '2011-11-02', 1, '13650891758', false);
insert into public.studentrelative (id, role, name, mobile) values (264, 'mother', '付小姐', '13650891758');
insert into public.student_studentrelative (student_id, relatives_id) values (263, 264);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (265, 'AK1207', null, '沈中浩', '2013-05-29', 0, '13650891758', false);
insert into public.studentrelative (id, role, name, mobile) values (266, 'mother', '付小姐', '13650891758');
insert into public.student_studentrelative (student_id, relatives_id) values (265, 266);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (267, 'AK1208', 'Jusly', '冯可卿', '2011-08-30', 1, '18680468666', false);
insert into public.studentrelative (id, role, name, mobile) values (268, 'mother', null, '18680468666');
insert into public.student_studentrelative (student_id, relatives_id) values (267, 268);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (269, 'AK1209', null, '冯冠群', '2013-01-16', 0, '18680468666', false);
insert into public.studentrelative (id, role, name, mobile) values (270, 'mother', null, '18680468666');
insert into public.student_studentrelative (student_id, relatives_id) values (269, 270);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (271, 'AK1210', 'Kitty', '冯美卿', '2009-05-01', 1, '18680468666', false);
insert into public.studentrelative (id, role, name, mobile) values (272, 'mother', null, '18680468666');
insert into public.student_studentrelative (student_id, relatives_id) values (271, 272);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (273, 'AK1211', 'Catherine', '黄琳淇', '2012-06-01', 1, '13925156234', false);
insert into public.studentrelative (id, role, name, mobile) values (274, 'father', '黄生', '13925156234');
insert into public.student_studentrelative (student_id, relatives_id) values (273, 274);
insert into public.studentrelative (id, role, name, mobile) values (275, 'mother', '苏小姐', '15521087833');
insert into public.student_studentrelative (student_id, relatives_id) values (273, 275);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (276, 'AK1212', 'Daniel', '黄星珲', '2012-06-01', 0, '13925156234', false);
insert into public.studentrelative (id, role, name, mobile) values (277, 'father', '黄生', '13925156234');
insert into public.student_studentrelative (student_id, relatives_id) values (276, 277);
insert into public.studentrelative (id, role, name, mobile) values (278, 'mother', '苏小姐', '15521087833');
insert into public.student_studentrelative (student_id, relatives_id) values (276, 278);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (279, 'AK1213', 'Demi', '谢心悦', '2013-09-02', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (280, 'AK1214', 'Yoyo', '谢佰佑', '2013-03-12', 1, '13925058848', false);
insert into public.studentrelative (id, role, name, mobile) values (281, 'father', '谢中华', '13925058848');
insert into public.student_studentrelative (student_id, relatives_id) values (280, 281);
insert into public.studentrelative (id, role, name, mobile) values (282, 'mother', '张西风', '18620063348');
insert into public.student_studentrelative (student_id, relatives_id) values (280, 282);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (283, 'AK1215', 'Yoyo', '张书瑶', '2011-08-25', 1, '13926461202', false);
insert into public.studentrelative (id, role, name, mobile) values (284, 'mother', '罗思诗，', '13926461202');
insert into public.student_studentrelative (student_id, relatives_id) values (283, 284);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (285, 'AK1216', 'Cici', '陶妍希', '2012-12-28', 1, '18578750825', false);
insert into public.studentrelative (id, role, name, mobile) values (286, 'mother', '许妍钰', '18578750825');
insert into public.student_studentrelative (student_id, relatives_id) values (285, 286);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (287, 'AK1217', null, '余冰蕙', '2013-09-25', 1, '13602787637', false);
insert into public.studentrelative (id, role, name, mobile) values (288, 'father', null, '13602787637');
insert into public.student_studentrelative (student_id, relatives_id) values (287, 288);
insert into public.studentrelative (id, role, name, mobile) values (289, 'mother', '张女士', '13926247107');
insert into public.student_studentrelative (student_id, relatives_id) values (287, 289);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (290, 'AK1218', null, '周宇桐', '2013-07-18', 1, null, false);
insert into public.studentrelative (id, role, name, mobile) values (291, 'mother', '冯女士（外婆）', null);
insert into public.student_studentrelative (student_id, relatives_id) values (290, 291);
insert into public.studentrelative (id, role, name, mobile) values (292, 'aunt', null, '13508035060');
insert into public.student_studentrelative (student_id, relatives_id) values (290, 292);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (293, 'AK1219', 'Osama', '徐奥森', '2012-11-07', 0, '15989000996', false);
insert into public.studentrelative (id, role, name, mobile) values (294, 'father', 'Esam', '15989000996');
insert into public.student_studentrelative (student_id, relatives_id) values (293, 294);
insert into public.studentrelative (id, role, name, mobile) values (295, 'mother', '徐雯', '13631331756');
insert into public.student_studentrelative (student_id, relatives_id) values (293, 295);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (296, 'AK1220', 'Molly', '黄楚涵', '2015-03-15', 1, '13600473004', false);
insert into public.studentrelative (id, role, name, mobile) values (297, 'father', '黄海', '13600473004');
insert into public.student_studentrelative (student_id, relatives_id) values (296, 297);
insert into public.studentrelative (id, role, name, mobile) values (298, 'mother', '李丹', '13924009939');
insert into public.student_studentrelative (student_id, relatives_id) values (296, 298);
insert into public.studentrelative (id, role, name, mobile) values (299, 'aunt', null, '13660045610');
insert into public.student_studentrelative (student_id, relatives_id) values (296, 299);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (300, 'AK1221', 'Jennifer', '蒋涵伊', '2012-04-15', 1, '15989180753', false);
insert into public.studentrelative (id, role, name, mobile) values (301, 'mother', '李延社', '15989180753');
insert into public.student_studentrelative (student_id, relatives_id) values (300, 301);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (302, 'AK1222', 'Kevin', '易薪宇', '2013-04-17', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (303, 'father', '易豪武', null);
insert into public.student_studentrelative (student_id, relatives_id) values (302, 303);
insert into public.studentrelative (id, role, name, mobile) values (304, 'mother', '秦会敏', '13825068252');
insert into public.student_studentrelative (student_id, relatives_id) values (302, 304);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (305, 'AK1223', 'Kiki', '元清', '2010-09-11', 1, '13802508666', false);
insert into public.studentrelative (id, role, name, mobile) values (306, 'father', '元晓峰', '13802508666');
insert into public.student_studentrelative (student_id, relatives_id) values (305, 306);
insert into public.studentrelative (id, role, name, mobile) values (307, 'mother', '皮婷婷', '13724843537');
insert into public.student_studentrelative (student_id, relatives_id) values (305, 307);
insert into public.studentrelative (id, role, name, mobile) values (308, 'aunt', null, '18927596090');
insert into public.student_studentrelative (student_id, relatives_id) values (305, 308);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (309, 'AK1224', 'Mandy', '向思艺', '2013-06-02', 1, '15975582536', false);
insert into public.studentrelative (id, role, name, mobile) values (310, 'mother', '万琴', '15975582536');
insert into public.student_studentrelative (student_id, relatives_id) values (309, 310);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (311, 'AK1225', 'Olivia Greene', null, null, 1, '13060648813', false);
insert into public.studentrelative (id, role, name, mobile) values (312, 'mother', 'Jenna Greene', '13060648813');
insert into public.student_studentrelative (student_id, relatives_id) values (311, 312);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (313, 'AK1226', null, '王可言', '2011-06-19', 0, '13825016446', false);
insert into public.studentrelative (id, role, name, mobile) values (314, 'father', '王鹏', '13825016446');
insert into public.student_studentrelative (student_id, relatives_id) values (313, 314);
insert into public.studentrelative (id, role, name, mobile) values (315, 'mother', '魏小姐', '13560176553');
insert into public.student_studentrelative (student_id, relatives_id) values (313, 315);
insert into public.studentrelative (id, role, name, mobile) values (316, 'aunt', '魏曼曼', '13560176553');
insert into public.student_studentrelative (student_id, relatives_id) values (313, 316);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (317, 'AK1227', 'Elin', '龚宣霖', '2013-01-12', 1, null, false);
insert into public.studentrelative (id, role, name, mobile) values (318, 'father', '龚承', null);
insert into public.student_studentrelative (student_id, relatives_id) values (317, 318);
insert into public.studentrelative (id, role, name, mobile) values (319, 'mother', '余娟', '13826458514');
insert into public.student_studentrelative (student_id, relatives_id) values (317, 319);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (320, 'AK1228', 'David', '陈禹衡', '2013-07-28', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (321, 'father', '陈俊晓', null);
insert into public.student_studentrelative (student_id, relatives_id) values (320, 321);
insert into public.studentrelative (id, role, name, mobile) values (322, 'mother', '洪子淇', '18665669593');
insert into public.student_studentrelative (student_id, relatives_id) values (320, 322);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (323, 'AK1229', 'Venus', '黄瑶瑶', '2013-01-18', 1, '13602889083', false);
insert into public.studentrelative (id, role, name, mobile) values (324, 'father', '秦女士', '13602889083');
insert into public.student_studentrelative (student_id, relatives_id) values (323, 324);
insert into public.studentrelative (id, role, name, mobile) values (325, 'mother', null, '15920397391');
insert into public.student_studentrelative (student_id, relatives_id) values (323, 325);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (326, 'AK1230', null, '陈正熙', '2014-08-16', 0, '15920580102', false);
insert into public.studentrelative (id, role, name, mobile) values (327, 'father', null, '15920580102');
insert into public.student_studentrelative (student_id, relatives_id) values (326, 327);
insert into public.studentrelative (id, role, name, mobile) values (328, 'mother', '张生', '13632313276');
insert into public.student_studentrelative (student_id, relatives_id) values (326, 328);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (329, 'AK1231', null, '林曾垚', '2012-12-17', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (330, 'father', '林先生', null);
insert into public.student_studentrelative (student_id, relatives_id) values (329, 330);
insert into public.studentrelative (id, role, name, mobile) values (331, 'mother', '曾女士', '13802990333');
insert into public.student_studentrelative (student_id, relatives_id) values (329, 331);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (332, 'AK1232', null, '林曾圻', '2012-12-17', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (333, 'father', '林先生', null);
insert into public.student_studentrelative (student_id, relatives_id) values (332, 333);
insert into public.studentrelative (id, role, name, mobile) values (334, 'mother', '曾女士', '13802990333');
insert into public.student_studentrelative (student_id, relatives_id) values (332, 334);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (335, 'AK1233', null, '马玲珑', '2011-10-01', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (336, 'AK1234', 'Elsa', '陈嫚庭', '2011-07-01', 1, '13802728178', false);
insert into public.studentrelative (id, role, name, mobile) values (337, 'mother', null, '13802728178');
insert into public.student_studentrelative (student_id, relatives_id) values (336, 337);
insert into public.studentrelative (id, role, name, mobile) values (338, 'aunt', null, '13642628551');
insert into public.student_studentrelative (student_id, relatives_id) values (336, 338);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (339, 'AK1235', 'Bobo', '陈嫚真', '2013-12-01', 1, '13802728178', false);
insert into public.studentrelative (id, role, name, mobile) values (340, 'mother', null, '13802728178');
insert into public.student_studentrelative (student_id, relatives_id) values (339, 340);
insert into public.studentrelative (id, role, name, mobile) values (341, 'aunt', null, '13642628551');
insert into public.student_studentrelative (student_id, relatives_id) values (339, 341);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (342, 'AK1236', null, '梁舜荣', '2012-02-05', 0, '13450272729', false);
insert into public.studentrelative (id, role, name, mobile) values (343, 'mother', 'Zoe', '13450272729');
insert into public.student_studentrelative (student_id, relatives_id) values (342, 343);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (344, 'AK1237', 'Sasa', '李沐璇', '2012-02-20', 1, null, false);
insert into public.studentrelative (id, role, name, mobile) values (345, 'father', '雷佳佳', null);
insert into public.student_studentrelative (student_id, relatives_id) values (344, 345);
insert into public.studentrelative (id, role, name, mobile) values (346, 'mother', '雷佳佳', '13826128951');
insert into public.student_studentrelative (student_id, relatives_id) values (344, 346);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (347, 'AK1238', 'Mini', '胡敏怡', null, 1, null, false);
insert into public.studentrelative (id, role, name, mobile) values (348, 'father', '邱凌', null);
insert into public.student_studentrelative (student_id, relatives_id) values (347, 348);
insert into public.studentrelative (id, role, name, mobile) values (349, 'mother', null, '13710636189');
insert into public.student_studentrelative (student_id, relatives_id) values (347, 349);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (350, 'AK1239', null, '朱怡陈', '2012-11-25', 1, '18688399599', false);
insert into public.studentrelative (id, role, name, mobile) values (351, 'father', '陈慧', '18688399599');
insert into public.student_studentrelative (student_id, relatives_id) values (350, 351);
insert into public.studentrelative (id, role, name, mobile) values (352, 'mother', null, '18680519028');
insert into public.student_studentrelative (student_id, relatives_id) values (350, 352);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (353, 'AK1240', 'Michelle', '朱雪怡', '2008-10-12', 1, '13802945389', false);
insert into public.studentrelative (id, role, name, mobile) values (354, 'mother', null, '13802945389');
insert into public.student_studentrelative (student_id, relatives_id) values (353, 354);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (355, 'AK1241', 'Rachel', '朱睿妍', '2012-09-22', 1, '13802945389', false);
insert into public.studentrelative (id, role, name, mobile) values (356, 'mother', null, '13802945389');
insert into public.student_studentrelative (student_id, relatives_id) values (355, 356);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (357, 'AK1242', 'Pye', '黄品淏', '2011-09-01', 0, '13527801640', false);
insert into public.studentrelative (id, role, name, mobile) values (358, 'mother', '周', '13527801640');
insert into public.student_studentrelative (student_id, relatives_id) values (357, 358);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (359, 'AK1243', 'Joy', '陈懿昕', '2011-11-01', 1, '18922351839', false);
insert into public.studentrelative (id, role, name, mobile) values (360, 'father', null, '18922351839');
insert into public.student_studentrelative (student_id, relatives_id) values (359, 360);
insert into public.studentrelative (id, role, name, mobile) values (361, 'mother', null, '18666077687');
insert into public.student_studentrelative (student_id, relatives_id) values (359, 361);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (362, 'AK1244', 'Faith', '陈懿佳', '2013-01-12', 1, '18922351839', false);
insert into public.studentrelative (id, role, name, mobile) values (363, 'father', null, '18922351839');
insert into public.student_studentrelative (student_id, relatives_id) values (362, 363);
insert into public.studentrelative (id, role, name, mobile) values (364, 'mother', null, '18666077687');
insert into public.student_studentrelative (student_id, relatives_id) values (362, 364);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (365, 'AK1245', 'Patrick', '吴梓沛', '2012-05-23', 0, '13570950493', false);
insert into public.studentrelative (id, role, name, mobile) values (366, 'mother', null, '13570950493');
insert into public.student_studentrelative (student_id, relatives_id) values (365, 366);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (367, 'AK1246', null, '吴梓熠', '2014-01-14', 0, '13570950493', false);
insert into public.studentrelative (id, role, name, mobile) values (368, 'mother', null, '13570950493');
insert into public.student_studentrelative (student_id, relatives_id) values (367, 368);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (369, 'AK1247', 'Coco', '李安珂', '2013-04-04', 1, '13632391970', false);
insert into public.studentrelative (id, role, name, mobile) values (370, 'mother', null, '13632391970');
insert into public.student_studentrelative (student_id, relatives_id) values (369, 370);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (371, 'AK1248', 'Pannie', '李霈恩', '2013-03-17', 1, '13560355714', false);
insert into public.studentrelative (id, role, name, mobile) values (372, 'father', '李生', '13560355714');
insert into public.student_studentrelative (student_id, relatives_id) values (371, 372);
insert into public.studentrelative (id, role, name, mobile) values (373, 'mother', '张女士', '15918740248');
insert into public.student_studentrelative (student_id, relatives_id) values (371, 373);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (374, 'AK1249', 'Nancy', '吴南逸', '2013-03-15', 1, '18926264131', false);
insert into public.studentrelative (id, role, name, mobile) values (375, 'father', null, '18926264131');
insert into public.student_studentrelative (student_id, relatives_id) values (374, 375);
insert into public.studentrelative (id, role, name, mobile) values (376, 'mother', '周嫦妮', '18926261314');
insert into public.student_studentrelative (student_id, relatives_id) values (374, 376);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (377, 'AK1250', 'Yale', '王顥烨', '2012-05-19', 1, '13632360886', false);
insert into public.studentrelative (id, role, name, mobile) values (378, 'father', '王定', '13632360886');
insert into public.student_studentrelative (student_id, relatives_id) values (377, 378);
insert into public.studentrelative (id, role, name, mobile) values (379, 'mother', null, '13570230606');
insert into public.student_studentrelative (student_id, relatives_id) values (377, 379);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (380, 'AK1251', 'Ellyssa Ariann', null, null, 1, '18664857072', false);
insert into public.studentrelative (id, role, name, mobile) values (381, 'father', null, '18664857072');
insert into public.student_studentrelative (student_id, relatives_id) values (380, 381);
insert into public.studentrelative (id, role, name, mobile) values (382, 'mother', 'Mama', '18664857027');
insert into public.student_studentrelative (student_id, relatives_id) values (380, 382);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (383, 'AK1252', 'Emran Arrazi', null, null, 0, '18664857072', false);
insert into public.studentrelative (id, role, name, mobile) values (384, 'father', null, '18664857072');
insert into public.student_studentrelative (student_id, relatives_id) values (383, 384);
insert into public.studentrelative (id, role, name, mobile) values (385, 'mother', 'Mama', '18664857027');
insert into public.student_studentrelative (student_id, relatives_id) values (383, 385);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (386, 'AK1253', null, '李念纯', '2013-12-31', 1, '13503001353', false);
insert into public.studentrelative (id, role, name, mobile) values (387, 'father', '林书舟', '13503001353');
insert into public.student_studentrelative (student_id, relatives_id) values (386, 387);
insert into public.studentrelative (id, role, name, mobile) values (388, 'mother', null, '13580332411');
insert into public.student_studentrelative (student_id, relatives_id) values (386, 388);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (389, 'AK1254', 'Joe', '李宗泽', '2012-10-01', 0, '13822120599', false);
insert into public.studentrelative (id, role, name, mobile) values (390, 'mother', '黄潇潇', '13822120599');
insert into public.student_studentrelative (student_id, relatives_id) values (389, 390);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (391, 'AK1255', null, '洪灵晞', '2012-03-24', 1, '1311188804', false);
insert into public.studentrelative (id, role, name, mobile) values (392, 'father', null, '1311188804');
insert into public.student_studentrelative (student_id, relatives_id) values (391, 392);
insert into public.studentrelative (id, role, name, mobile) values (393, 'mother', null, '15000088185');
insert into public.student_studentrelative (student_id, relatives_id) values (391, 393);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (394, 'AK1256', 'Sam', '周宇轩', '2009-03-05', 0, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (395, 'AK1257', 'Allen', '周宇扬', '2011-05-02', 0, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (396, 'AK1258', 'Kitty', '周语欣', '2013-03-01', 1, '18664226261', false);
insert into public.studentrelative (id, role, name, mobile) values (397, 'father', '周丹', '18664226261');
insert into public.student_studentrelative (student_id, relatives_id) values (396, 397);
insert into public.studentrelative (id, role, name, mobile) values (398, 'mother', 'Kitty', '13580332288');
insert into public.student_studentrelative (student_id, relatives_id) values (396, 398);
insert into public.studentrelative (id, role, name, mobile) values (399, 'aunt', '阿芳', '186-6422-6261');
insert into public.student_studentrelative (student_id, relatives_id) values (396, 399);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (400, 'AK1259', 'William', '王子', '2012-09-17', 0, '13802849939', false);
insert into public.studentrelative (id, role, name, mobile) values (401, 'mother', null, '13802849939');
insert into public.student_studentrelative (student_id, relatives_id) values (400, 401);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (402, 'AK1260', 'Oscar', '李牧谦', '2013-09-03', 0, '13602818988', false);
insert into public.studentrelative (id, role, name, mobile) values (403, 'father', '李川', '13602818988');
insert into public.student_studentrelative (student_id, relatives_id) values (402, 403);
insert into public.studentrelative (id, role, name, mobile) values (404, 'mother', '妈妈', '18688444733');
insert into public.student_studentrelative (student_id, relatives_id) values (402, 404);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (405, 'AK1261', 'Bella', '李牧瑶', '2013-09-03', 1, '13602818988', false);
insert into public.studentrelative (id, role, name, mobile) values (406, 'father', '李川', '13602818988');
insert into public.student_studentrelative (student_id, relatives_id) values (405, 406);
insert into public.studentrelative (id, role, name, mobile) values (407, 'mother', '妈妈', '18688444733');
insert into public.student_studentrelative (student_id, relatives_id) values (405, 407);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (408, 'AK1262', 'Shin', '廖信宇', '2013-01-25', 0, '15627869910', false);
insert into public.studentrelative (id, role, name, mobile) values (409, 'father', null, '15627869910');
insert into public.student_studentrelative (student_id, relatives_id) values (408, 409);
insert into public.studentrelative (id, role, name, mobile) values (410, 'mother', null, '15627867070');
insert into public.student_studentrelative (student_id, relatives_id) values (408, 410);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (411, 'AK1263', 'Cicy', '吴欣晓', '2012-01-23', 1, '15989263389', false);
insert into public.studentrelative (id, role, name, mobile) values (412, 'mother', null, '15989263389');
insert into public.student_studentrelative (student_id, relatives_id) values (411, 412);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (413, 'AK1264', 'Ada', '彭瑾媛', '2012-03-01', 1, '13711355318', false);
insert into public.studentrelative (id, role, name, mobile) values (414, 'father', null, '13711355318');
insert into public.student_studentrelative (student_id, relatives_id) values (413, 414);
insert into public.studentrelative (id, role, name, mobile) values (415, 'mother', null, '18126811691');
insert into public.student_studentrelative (student_id, relatives_id) values (413, 415);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (416, 'AK1265', null, '陈陈', '2015-01-15', 1, '13922704765', false);
insert into public.studentrelative (id, role, name, mobile) values (417, 'father', '唐文伟', '13922704765');
insert into public.student_studentrelative (student_id, relatives_id) values (416, 417);
insert into public.studentrelative (id, role, name, mobile) values (418, 'mother', '陈艳', '13660139805');
insert into public.student_studentrelative (student_id, relatives_id) values (416, 418);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (419, 'AK1266', 'Tiger', '陈中', '2010-04-02', 0, '18602017229', false);
insert into public.studentrelative (id, role, name, mobile) values (420, 'father', '陈先生', '18602017229');
insert into public.student_studentrelative (student_id, relatives_id) values (419, 420);
insert into public.studentrelative (id, role, name, mobile) values (421, 'mother', null, '13249658229');
insert into public.student_studentrelative (student_id, relatives_id) values (419, 421);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (422, 'AK1267', 'Jennifer', '周美慧', '2012-12-30', 1, '18600049888', false);
insert into public.studentrelative (id, role, name, mobile) values (423, 'father', '周丹', '18600049888');
insert into public.student_studentrelative (student_id, relatives_id) values (422, 423);
insert into public.studentrelative (id, role, name, mobile) values (424, 'mother', null, '13870680738');
insert into public.student_studentrelative (student_id, relatives_id) values (422, 424);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (425, 'AK1268', null, '李小垚', '2013-12-29', 1, '18988990260', false);
insert into public.studentrelative (id, role, name, mobile) values (426, 'mother', null, '18988990260');
insert into public.student_studentrelative (student_id, relatives_id) values (425, 426);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (427, 'AK1269', 'Dennis', '杨东升', '2010-07-04', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (428, 'father', '赵红玲', null);
insert into public.student_studentrelative (student_id, relatives_id) values (427, 428);
insert into public.studentrelative (id, role, name, mobile) values (429, 'mother', null, '13570071288');
insert into public.student_studentrelative (student_id, relatives_id) values (427, 429);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (430, 'AK1270', null, '杨东旭', '2015-05-17', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (431, 'father', '赵红玲', null);
insert into public.student_studentrelative (student_id, relatives_id) values (430, 431);
insert into public.studentrelative (id, role, name, mobile) values (432, 'mother', null, '13570071288');
insert into public.student_studentrelative (student_id, relatives_id) values (430, 432);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (433, 'AK1271', 'Cassie', null, '2012-07-04', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (434, 'AK1272', 'Mango', '吕奕晓', '2012-09-18', 1, '13922290925', false);
insert into public.studentrelative (id, role, name, mobile) values (435, 'father', '宋女士', '13922290925');
insert into public.student_studentrelative (student_id, relatives_id) values (434, 435);
insert into public.studentrelative (id, role, name, mobile) values (436, 'mother', '吕生', '13926167223');
insert into public.student_studentrelative (student_id, relatives_id) values (434, 436);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (437, 'AK1273', 'Helen', '张筱曼', '2011-06-22', 1, '13926495663', false);
insert into public.studentrelative (id, role, name, mobile) values (438, 'mother', null, '13926495663');
insert into public.student_studentrelative (student_id, relatives_id) values (437, 438);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (439, 'AK1274', 'Dora', '朱昱燃', '2012-11-18', 1, null, false);
insert into public.studentrelative (id, role, name, mobile) values (440, 'father', '伍女士', null);
insert into public.student_studentrelative (student_id, relatives_id) values (439, 440);
insert into public.studentrelative (id, role, name, mobile) values (441, 'mother', null, '13570366032');
insert into public.student_studentrelative (student_id, relatives_id) values (439, 441);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (442, 'AK1275', null, '许卿怡', '2011-12-01', 1, '15112110540', false);
insert into public.studentrelative (id, role, name, mobile) values (443, 'mother', null, '15112110540');
insert into public.student_studentrelative (student_id, relatives_id) values (442, 443);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (444, 'AK1276', 'Rabbit', '吴星瑶', '2012-04-20', 1, null, false);
insert into public.studentrelative (id, role, name, mobile) values (445, 'father', '李艳', null);
insert into public.student_studentrelative (student_id, relatives_id) values (444, 445);
insert into public.studentrelative (id, role, name, mobile) values (446, 'mother', null, '18011942098');
insert into public.student_studentrelative (student_id, relatives_id) values (444, 446);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (447, 'AK1277', null, '彭佳鑫', '2009-08-06', 0, '15112110540', false);
insert into public.studentrelative (id, role, name, mobile) values (448, 'mother', null, '15112110540');
insert into public.student_studentrelative (student_id, relatives_id) values (447, 448);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (449, 'AK1278', 'Howard', '梁芷滔', '2013-12-12', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (450, 'father', '谭惠艳', null);
insert into public.student_studentrelative (student_id, relatives_id) values (449, 450);
insert into public.studentrelative (id, role, name, mobile) values (451, 'mother', '谭惠艳', '18820071866');
insert into public.student_studentrelative (student_id, relatives_id) values (449, 451);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (452, 'AK1279', null, null, null, 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (453, 'AK1280', 'Kunhu Ahn', null, null, 0, '15918549685', false);
insert into public.studentrelative (id, role, name, mobile) values (454, 'father', null, '15918549685');
insert into public.student_studentrelative (student_id, relatives_id) values (453, 454);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (455, 'AK1281', 'William', null, '2013-12-17', 0, '18565335452', false);
insert into public.studentrelative (id, role, name, mobile) values (456, 'mother', null, '18565335452');
insert into public.student_studentrelative (student_id, relatives_id) values (455, 456);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (457, 'AK1282', 'Samuel', null, '2011-04-30', 0, '18565335452', false);
insert into public.studentrelative (id, role, name, mobile) values (458, 'mother', null, '18565335452');
insert into public.student_studentrelative (student_id, relatives_id) values (457, 458);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (459, 'AK1283', 'Emma', '李芷瑜', '2012-08-04', 1, '13560145448', false);
insert into public.studentrelative (id, role, name, mobile) values (460, 'mother', null, '13560145448');
insert into public.student_studentrelative (student_id, relatives_id) values (459, 460);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (461, 'AK1284', 'Barry', '李天瑜', '2014-09-19', 0, '13560145448', false);
insert into public.studentrelative (id, role, name, mobile) values (462, 'mother', null, '13560145448');
insert into public.student_studentrelative (student_id, relatives_id) values (461, 462);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (463, 'AK1285', null, '李启瑞', '2013-08-26', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (464, 'father', '邝小姐', null);
insert into public.student_studentrelative (student_id, relatives_id) values (463, 464);
insert into public.studentrelative (id, role, name, mobile) values (465, 'mother', null, '13632255442');
insert into public.student_studentrelative (student_id, relatives_id) values (463, 465);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (466, 'AK1286', 'Rebecca', '郭梓萱', '2013-05-06', 1, '18898493832', false);
insert into public.studentrelative (id, role, name, mobile) values (467, 'father', null, '18898493832');
insert into public.student_studentrelative (student_id, relatives_id) values (466, 467);
insert into public.studentrelative (id, role, name, mobile) values (468, 'mother', null, '15113883832');
insert into public.student_studentrelative (student_id, relatives_id) values (466, 468);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (469, 'AK1287', null, '彭子蕙', '2014-07-12', 1, '18620610212', false);
insert into public.studentrelative (id, role, name, mobile) values (470, 'mother', null, '18620610212');
insert into public.student_studentrelative (student_id, relatives_id) values (469, 470);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (471, 'AK1288', 'Cindy', '李思颖', '2009-02-02', 1, '18675841099', false);
insert into public.studentrelative (id, role, name, mobile) values (472, 'father', '李先生', '18675841099');
insert into public.student_studentrelative (student_id, relatives_id) values (471, 472);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (473, 'AK1289', 'Henry', '李鸿毅', '2013-12-02', 0, '18675841099', false);
insert into public.studentrelative (id, role, name, mobile) values (474, 'father', '李先生', '18675841099');
insert into public.student_studentrelative (student_id, relatives_id) values (473, 474);
insert into public.studentrelative (id, role, name, mobile) values (475, 'mother', null, '18675841099');
insert into public.student_studentrelative (student_id, relatives_id) values (473, 475);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (476, 'AK1290', 'Ding Ding', '丁丁', '2012-11-04', 1, '18922203003', false);
insert into public.studentrelative (id, role, name, mobile) values (477, 'mother', null, '18922203003');
insert into public.student_studentrelative (student_id, relatives_id) values (476, 477);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (478, 'AK1291', 'Julia', null, '2013-10-22', 1, '13902302300', false);
insert into public.studentrelative (id, role, name, mobile) values (479, 'father', 'John', '13902302300');
insert into public.student_studentrelative (student_id, relatives_id) values (478, 479);
insert into public.studentrelative (id, role, name, mobile) values (480, 'mother', 'Elya', '15099957745');
insert into public.student_studentrelative (student_id, relatives_id) values (478, 480);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (481, 'AK1292', null, '庄迪嘉', '2013-11-13', 0, '13570305541', false);
insert into public.studentrelative (id, role, name, mobile) values (482, 'father', '爸爸', '13570305541');
insert into public.student_studentrelative (student_id, relatives_id) values (481, 482);
insert into public.studentrelative (id, role, name, mobile) values (483, 'mother', '母亲', '15024287477');
insert into public.student_studentrelative (student_id, relatives_id) values (481, 483);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (484, 'AK1293', null, '晏然', '2013-09-24', 0, '13556001644', false);
insert into public.studentrelative (id, role, name, mobile) values (485, 'father', null, '13556001644');
insert into public.student_studentrelative (student_id, relatives_id) values (484, 485);
insert into public.studentrelative (id, role, name, mobile) values (486, 'mother', null, '13710380550');
insert into public.student_studentrelative (student_id, relatives_id) values (484, 486);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (487, 'AK1294', 'Alice', '杨予涵', '2013-06-18', 1, '13660619269', false);
insert into public.studentrelative (id, role, name, mobile) values (488, 'father', '杨李欣', '13660619269');
insert into public.student_studentrelative (student_id, relatives_id) values (487, 488);
insert into public.studentrelative (id, role, name, mobile) values (489, 'mother', '付丽明', '13632412706');
insert into public.student_studentrelative (student_id, relatives_id) values (487, 489);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (490, 'AK1295', 'Paris', '杨予熙', '2013-06-18', 1, '13660619269', false);
insert into public.studentrelative (id, role, name, mobile) values (491, 'father', '杨李欣', '13660619269');
insert into public.student_studentrelative (student_id, relatives_id) values (490, 491);
insert into public.studentrelative (id, role, name, mobile) values (492, 'mother', '付丽明', '13632412706');
insert into public.student_studentrelative (student_id, relatives_id) values (490, 492);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (493, 'AK1296', 'Meixi', '林美西', '2011-11-20', 1, '13570103787', false);
insert into public.studentrelative (id, role, name, mobile) values (494, 'father', '林美西', '13570103787');
insert into public.student_studentrelative (student_id, relatives_id) values (493, 494);
insert into public.studentrelative (id, role, name, mobile) values (495, 'mother', '林美西', '13826495800');
insert into public.student_studentrelative (student_id, relatives_id) values (493, 495);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (496, 'AK1297', null, null, null, 1, '13570103787', false);
insert into public.studentrelative (id, role, name, mobile) values (497, 'father', '林美西', '13570103787');
insert into public.student_studentrelative (student_id, relatives_id) values (496, 497);
insert into public.studentrelative (id, role, name, mobile) values (498, 'mother', '林美西', '13826495800');
insert into public.student_studentrelative (student_id, relatives_id) values (496, 498);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (499, 'AK1298', 'Anne', '杨安妮', '2012-06-03', 1, null, false);
insert into public.studentrelative (id, role, name, mobile) values (500, 'mother', '18664675120', null);
insert into public.student_studentrelative (student_id, relatives_id) values (499, 500);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (501, 'AK1299', 'Ameilia', '赵珊慧', '2010-10-26', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (502, 'AK1300', 'Darrick', '赵冠霖', '2014-06-10', 0, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (503, 'AK1301', 'Kerr', '赵可', '2012-01-01', 1, '13926088011', false);
insert into public.studentrelative (id, role, name, mobile) values (504, 'mother', '赵可', '13926088011');
insert into public.student_studentrelative (student_id, relatives_id) values (503, 504);
insert into public.studentrelative (id, role, name, mobile) values (505, 'aunt', '赵可', '13544522154');
insert into public.student_studentrelative (student_id, relatives_id) values (503, 505);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (506, 'AK1302', null, '李欣欣', '2013-03-01', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (507, 'AK1303', 'Pandora', '潘晨雨', '2012-04-19', 1, '18680549992', false);
insert into public.studentrelative (id, role, name, mobile) values (508, 'father', '潘益华', '18680549992');
insert into public.student_studentrelative (student_id, relatives_id) values (507, 508);
insert into public.studentrelative (id, role, name, mobile) values (509, 'mother', '金玮', '13763397776');
insert into public.student_studentrelative (student_id, relatives_id) values (507, 509);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (510, 'AK1304', 'Martin', '吴昱泽', '2012-04-18', 0, '18688208511', false);
insert into public.studentrelative (id, role, name, mobile) values (511, 'father', '吴旭波', '18688208511');
insert into public.student_studentrelative (student_id, relatives_id) values (510, 511);
insert into public.studentrelative (id, role, name, mobile) values (512, 'mother', '李花', '15999952323');
insert into public.student_studentrelative (student_id, relatives_id) values (510, 512);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (513, 'AK1305', 'Do Do', '朵朵,', '2012-09-30', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (514, 'AK1306', null, '杨恒', '2013-06-08', 1, '15102019184', false);
insert into public.studentrelative (id, role, name, mobile) values (515, 'father', '爸爸', '15102019184');
insert into public.student_studentrelative (student_id, relatives_id) values (514, 515);
insert into public.studentrelative (id, role, name, mobile) values (516, 'mother', '李红辉', '13631345077');
insert into public.student_studentrelative (student_id, relatives_id) values (514, 516);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (517, 'AK1307', null, '马楠馨', '2013-04-21', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (518, 'AK1308', 'Abby', '佟琳,', '2012-07-11', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (519, 'AK1309', 'Angel', '简匡霆', '2011-10-05', 0, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (520, 'AK1310', null, '吴修美嘉', '2012-03-11', 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (521, 'AK1311', null, '許嘉軒', '2013-12-14', 0, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (522, 'AK1312', null, '刘泓锐', null, 0, '15800033397', false);
insert into public.studentrelative (id, role, name, mobile) values (523, 'father', '爸爸', '15800033397');
insert into public.student_studentrelative (student_id, relatives_id) values (522, 523);
insert into public.studentrelative (id, role, name, mobile) values (524, 'mother', '妈妈', '13710099299');
insert into public.student_studentrelative (student_id, relatives_id) values (522, 524);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (525, 'AK1313', 'Kayla', '苏烨琳', '2012-10-05', 1, '13570368821', false);
insert into public.studentrelative (id, role, name, mobile) values (526, 'father', '苏志伟', '13570368821');
insert into public.student_studentrelative (student_id, relatives_id) values (525, 526);
insert into public.studentrelative (id, role, name, mobile) values (527, 'mother', '陈婷婷', '13570339219');
insert into public.student_studentrelative (student_id, relatives_id) values (525, 527);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (528, 'AK1314', null, '苏鑫浩', '2014-06-25', 0, '13570368821', false);
insert into public.studentrelative (id, role, name, mobile) values (529, 'father', '苏志伟', '13570368821');
insert into public.student_studentrelative (student_id, relatives_id) values (528, 529);
insert into public.studentrelative (id, role, name, mobile) values (530, 'mother', '陈婷婷', '13570339219');
insert into public.student_studentrelative (student_id, relatives_id) values (528, 530);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (531, 'AK1315', 'Bosco', '黄泽熙', '2011-07-11', 0, '13609042398', false);
insert into public.studentrelative (id, role, name, mobile) values (532, 'father', '黄铭隆', '13609042398');
insert into public.student_studentrelative (student_id, relatives_id) values (531, 532);
insert into public.studentrelative (id, role, name, mobile) values (533, 'mother', '吴蝶明', '13560109812');
insert into public.student_studentrelative (student_id, relatives_id) values (531, 533);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (534, 'AK1316', null, null, null, 0, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (535, 'AK1317', 'Nancy', '肖亚男', '2014-10-31', 1, null, false);
insert into public.studentrelative (id, role, name, mobile) values (536, 'father', '陈小平', null);
insert into public.student_studentrelative (student_id, relatives_id) values (535, 536);
insert into public.studentrelative (id, role, name, mobile) values (537, 'mother', '肖若成', null);
insert into public.student_studentrelative (student_id, relatives_id) values (535, 537);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (538, 'AK1318', 'Kevin', '肖云凯', '2011-04-18', 0, null, false);
insert into public.studentrelative (id, role, name, mobile) values (539, 'father', '陈小平', null);
insert into public.student_studentrelative (student_id, relatives_id) values (538, 539);
insert into public.studentrelative (id, role, name, mobile) values (540, 'mother', '肖若成', null);
insert into public.student_studentrelative (student_id, relatives_id) values (538, 540);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (541, 'AK1319', 'Nicole', '刘懿娴，', '2013-08-20', 1, '18602033158', false);
insert into public.studentrelative (id, role, name, mobile) values (542, 'father', '父亲', '18602033158');
insert into public.student_studentrelative (student_id, relatives_id) values (541, 542);
insert into public.studentrelative (id, role, name, mobile) values (543, 'mother', '吴', '13226681010');
insert into public.student_studentrelative (student_id, relatives_id) values (541, 543);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (544, 'AK1320', 'Emma', null, null, 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (545, 'AK1321', null, '芊芊', '2013-01-01', 1, '18665698015', false);
insert into public.studentrelative (id, role, name, mobile) values (546, 'father', '秦昊', '18665698015');
insert into public.student_studentrelative (student_id, relatives_id) values (545, 546);
insert into public.studentrelative (id, role, name, mobile) values (547, 'mother', '黄倚心', '18898458015');
insert into public.student_studentrelative (student_id, relatives_id) values (545, 547);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (548, 'AK1322', 'Abbegail', '蔡依纯', '2011-11-30', 1, '13826100448', false);
insert into public.studentrelative (id, role, name, mobile) values (549, 'father', '李佳', '13826100448');
insert into public.student_studentrelative (student_id, relatives_id) values (548, 549);
insert into public.studentrelative (id, role, name, mobile) values (550, 'mother', '李佳', '13826100448');
insert into public.student_studentrelative (student_id, relatives_id) values (548, 550);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (551, 'AK1323', null, '林芷心', '2012-08-24', 1, '13560354945', false);
insert into public.studentrelative (id, role, name, mobile) values (552, 'father', '林硕', '13560354945');
insert into public.student_studentrelative (student_id, relatives_id) values (551, 552);
insert into public.studentrelative (id, role, name, mobile) values (553, 'mother', '熊雪霜', '13711236733');
insert into public.student_studentrelative (student_id, relatives_id) values (551, 553);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (554, 'AK1324', 'Tim', '蔡汶静', '2012-08-31', 0, '13318820178', false);
insert into public.studentrelative (id, role, name, mobile) values (555, 'father', '杨洋', '13318820178');
insert into public.student_studentrelative (student_id, relatives_id) values (554, 555);
insert into public.studentrelative (id, role, name, mobile) values (556, 'mother', '蔡汶静', '15915715896');
insert into public.student_studentrelative (student_id, relatives_id) values (554, 556);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (557, 'AK1325', 'Silvia', '黄婧怡', '2014-01-23', 1, '18620250808', false);
insert into public.studentrelative (id, role, name, mobile) values (558, 'father', '黄孝远', '18620250808');
insert into public.student_studentrelative (student_id, relatives_id) values (557, 558);
insert into public.studentrelative (id, role, name, mobile) values (559, 'mother', '刘晶瑜', '15013501388');
insert into public.student_studentrelative (student_id, relatives_id) values (557, 559);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (560, 'AK1326', 'Rena Ishikawa', null, null, 1, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (561, 'AK1327', 'Angeles', '张欣蔚', null, 1, '13926046345', false);
insert into public.studentrelative (id, role, name, mobile) values (562, 'father', 'Winson', '13926046345');
insert into public.student_studentrelative (student_id, relatives_id) values (561, 562);
insert into public.studentrelative (id, role, name, mobile) values (563, 'mother', 'Peggy', '13925017391');
insert into public.student_studentrelative (student_id, relatives_id) values (561, 563);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (564, 'AK1328', null, '余律铭', '2012-12-05', 0, '13711734818', false);
insert into public.studentrelative (id, role, name, mobile) values (565, 'father', '余盛珏', '13711734818');
insert into public.student_studentrelative (student_id, relatives_id) values (564, 565);
insert into public.studentrelative (id, role, name, mobile) values (566, 'mother', '方小秋', '13688891978');
insert into public.student_studentrelative (student_id, relatives_id) values (564, 566);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (567, 'AK1329', 'Yedda', '黄檍澄', '2014-02-14', 1, '13822262853', false);
insert into public.studentrelative (id, role, name, mobile) values (568, 'mother', '妈妈', '13822262853');
insert into public.student_studentrelative (student_id, relatives_id) values (567, 568);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (569, 'AK1330', null, null, null, 0, '13822262853', false);
insert into public.studentrelative (id, role, name, mobile) values (570, 'mother', '妈妈', '13822262853');
insert into public.student_studentrelative (student_id, relatives_id) values (569, 570);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (571, 'AK1331', 'Anna', null, '2009-01-06', 1, '18688885622', false);
insert into public.studentrelative (id, role, name, mobile) values (572, 'father', '郑博文', '18688885622');
insert into public.student_studentrelative (student_id, relatives_id) values (571, 572);
insert into public.studentrelative (id, role, name, mobile) values (573, 'mother', '陈晓风', '18665068822');
insert into public.student_studentrelative (student_id, relatives_id) values (571, 573);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (574, 'AK1332', 'Anne', null, '2009-01-06', 1, '18688885622', false);
insert into public.studentrelative (id, role, name, mobile) values (575, 'father', '郑博文', '18688885622');
insert into public.student_studentrelative (student_id, relatives_id) values (574, 575);
insert into public.studentrelative (id, role, name, mobile) values (576, 'mother', '陈晓风', '18665068822');
insert into public.student_studentrelative (student_id, relatives_id) values (574, 576);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (577, 'AK1333', 'Fiona', null, '2012-05-02', 1, '13822189888', false);
insert into public.studentrelative (id, role, name, mobile) values (578, 'father', '钟小锋', '13822189888');
insert into public.student_studentrelative (student_id, relatives_id) values (577, 578);
insert into public.studentrelative (id, role, name, mobile) values (579, 'mother', '李细英', '18588896913');
insert into public.student_studentrelative (student_id, relatives_id) values (577, 579);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (580, 'AK1334', 'Rebecca', '朱翼雯', '2011-05-15', 1, '18688888478', false);
insert into public.studentrelative (id, role, name, mobile) values (581, 'father', '朱爸爸', '18688888478');
insert into public.student_studentrelative (student_id, relatives_id) values (580, 581);
insert into public.studentrelative (id, role, name, mobile) values (582, 'mother', '李妈咪', '18688481801');
insert into public.student_studentrelative (student_id, relatives_id) values (580, 582);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (583, 'AK1335', 'Mavis', '朱翼旻', '2013-09-26', 1, '18688888478', false);
insert into public.studentrelative (id, role, name, mobile) values (584, 'father', '朱爸爸', '18688888478');
insert into public.student_studentrelative (student_id, relatives_id) values (583, 584);
insert into public.studentrelative (id, role, name, mobile) values (585, 'mother', '李妈咪', '18688481801');
insert into public.student_studentrelative (student_id, relatives_id) values (583, 585);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (586, 'AK1336', null, '高语谦', '2013-09-23', 1, '18588582006', false);
insert into public.studentrelative (id, role, name, mobile) values (587, 'father', null, '18588582006');
insert into public.student_studentrelative (student_id, relatives_id) values (586, 587);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (588, 'AK1337', 'Simon', '汪宜航', '2008-03-18', 0, '13819809590', false);
insert into public.studentrelative (id, role, name, mobile) values (589, 'father', 'Harry', '13819809590');
insert into public.student_studentrelative (student_id, relatives_id) values (588, 589);
insert into public.studentrelative (id, role, name, mobile) values (590, 'mother', null, '13570065732');
insert into public.student_studentrelative (student_id, relatives_id) values (588, 590);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (591, 'AK1338', 'Jason', '汪仁哲', '2013-05-31', 0, '13819809590', false);
insert into public.studentrelative (id, role, name, mobile) values (592, 'father', 'Harry', '13819809590');
insert into public.student_studentrelative (student_id, relatives_id) values (591, 592);
insert into public.studentrelative (id, role, name, mobile) values (593, 'mother', null, '13570065732');
insert into public.student_studentrelative (student_id, relatives_id) values (591, 593);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (594, 'AK1339', 'Even', '江毅丰', null, 0, '13715999333', false);
insert into public.studentrelative (id, role, name, mobile) values (595, 'father', '张少微', '13715999333');
insert into public.student_studentrelative (student_id, relatives_id) values (594, 595);
insert into public.studentrelative (id, role, name, mobile) values (596, 'mother', '张玲', '13592826981');
insert into public.student_studentrelative (student_id, relatives_id) values (594, 596);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (597, 'AK1340', 'Kiki', '蔡静錡', '2012-05-02', 1, '13422480388', false);
insert into public.studentrelative (id, role, name, mobile) values (598, 'father', '陈嘉玲', '13422480388');
insert into public.student_studentrelative (student_id, relatives_id) values (597, 598);
insert into public.studentrelative (id, role, name, mobile) values (599, 'mother', '蔡泽洪', '13570322935');
insert into public.student_studentrelative (student_id, relatives_id) values (597, 599);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (600, 'AK1341', 'Jason', '阳阳', '2013-04-04', 0, '13922201148', false);
insert into public.studentrelative (id, role, name, mobile) values (601, 'father', '爸爸', '13922201148');
insert into public.student_studentrelative (student_id, relatives_id) values (600, 601);
insert into public.studentrelative (id, role, name, mobile) values (602, 'mother', '陈林', '13822122084');
insert into public.student_studentrelative (student_id, relatives_id) values (600, 602);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (603, 'AK1342', 'Joyce', '蒋一涵', '2012-11-04', 1, '15813334538', false);
insert into public.studentrelative (id, role, name, mobile) values (604, 'father', '蒋智慧', '15813334538');
insert into public.student_studentrelative (student_id, relatives_id) values (603, 604);
insert into public.studentrelative (id, role, name, mobile) values (605, 'mother', '谌君', '15813315768');
insert into public.student_studentrelative (student_id, relatives_id) values (603, 605);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (606, 'AK1343', 'Yvonne', '贺一朗', '2012-08-21', 1, '13751711505', false);
insert into public.studentrelative (id, role, name, mobile) values (607, 'father', '贺小波', '13751711505');
insert into public.student_studentrelative (student_id, relatives_id) values (606, 607);
insert into public.studentrelative (id, role, name, mobile) values (608, 'mother', '芦俊媚', '13560138890');
insert into public.student_studentrelative (student_id, relatives_id) values (606, 608);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (609, 'AK1344', null, null, null, 0, null, false);

insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) values (610, 'AK1345', 'Leo', '李泳铭', '2011-03-08', 0, '13533250326', false);
insert into public.studentrelative (id, role, name, mobile) values (611, 'mother', '张莹', '13533250326');
insert into public.student_studentrelative (student_id, relatives_id) values (610, 611);

select setval('public.hibernate_sequence', 611);
