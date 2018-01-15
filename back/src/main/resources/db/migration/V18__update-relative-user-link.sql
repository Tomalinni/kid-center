insert into studentrelativerole (id, "name") values(1, '亲');
alter table appuser add column relative_id bigint;
alter table appuser add constraint appuser_relative_id_fkey foreign key (relative_id) references studentrelative (id);