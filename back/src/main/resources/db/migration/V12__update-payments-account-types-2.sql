alter table account rename accounttype to type;
insert into school (id, name, city_id, haslessons) values (-1, '不', null, false);
alter table account drop column internal;
update account set school_id=-1 where school_id is null;
alter table payment add column direction integer default 0;
