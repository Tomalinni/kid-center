insert into public.city (id, name) values (1, '广州');
insert into public.school (id, city_id, name) values (1, 1, '店名');
insert into public.account (id, school_id, city_id, bank, department, owner, number) values (1, 1, 1, '农业', '金穗路', 'zenchenkosergey', '208777808876');
insert into public.account (id, school_id, city_id, bank, department, owner, number) values (2, 1, 1, '农业', '金穗路', 'zenchenkosergey', '208777808877');

insert into public.category (id, parent_id, level, name) values (1, null, 0, '装修');
insert into public.category (id, parent_id, level, name) values (2, null, 0, '新水');
insert into public.category (id, parent_id, level, name) values (3, null, 0, '租金');
insert into public.category (id, parent_id, level, name) values (4, null, 0, '推广');
insert into public.category (id, parent_id, level, name) values (5, null, 0, '提成');
insert into public.category (id, parent_id, level, name) values (6, null, 0, '辅料');
insert into public.category (id, parent_id, level, name) values (7, null, 0, '设备');
insert into public.category (id, parent_id, level, name) values (8, null, 0, '活动');
insert into public.category (id, parent_id, level, name) values (9, null, 0, '开发');
insert into public.category (id, parent_id, level, name) values (10, null, 0, '出差');
insert into public.category (id, parent_id, level, name) values (11, null, 0, '训练');
insert into public.category (id, parent_id, level, name) values (12, null, 0, '动画');

select setval('public.category_seq', (select max(id) from category));
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 1, 1, '装饰');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 1, 1, '洁具');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 1, 1, '新水');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 1, 1, '设计');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 1, 1, '消防');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 1, 1, '砌墙');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 1, 1, '拉电');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 1, 1, '污水');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 1, 1, '刷漆');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 1, 1, '红包');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 2, 1, '老师');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 2, 1, '助理');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 2, 1, '前台');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 2, 1, '营销');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 2, 1, '校长');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 2, 1, '阿姨');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 2, 1, '司机');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 3, 1, '学校');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 3, 1, '房子');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 3, 1, '车子');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 3, 1, '设备');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 4, 1, '微信');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 4, 1, '网络');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 4, 1, '目录');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 4, 1, '宣传');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 4, 1, '电视');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 4, 1, '活动');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 5, 1, '老师');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 5, 1, '助理');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 5, 1, '前台');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 5, 1, '营销');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 5, 1, '校长');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 5, 1, '阿姨');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 5, 1, '司机');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 6, 1, '教室');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 6, 1, '外室');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 7, 1, '家具');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 7, 1, '设备');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 7, 1, '教材');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 7, 1, '衣服');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 7, 1, '食物');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 8, 1, '新年');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 8, 1, '五一');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 8, 1, '六节');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 8, 1, '圣诞');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 8, 1, '春节');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 8, 1, '中秋');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 8, 1, '周年');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 9, 1, '数据');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 9, 1, '教材');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 10, 1, '机票');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 10, 1, '火车票');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 10, 1, '酒店');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 10, 1, '请客');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 10, 1, '签证');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 10, 1, '保险');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 11, 1, '英语');

insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 12, 1, '美术');
insert into public.category (id, parent_id, level, name) values (nextval('category_seq'), 12, 1, '广告');

select setval('public.city_seq', (select max(id) from city));
select setval('public.school_seq', (select max(id) from school));
select setval('public.account_seq', (select max(id) from account));
select setval('public.payment_seq', (select max(id) from payment));
