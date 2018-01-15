alter table payment add column school_id bigint;
alter table payment add constraint payment_school_id_fkey foreign key (school_id) references school (id);
alter table payment add column target_school_id bigint;
alter table payment add constraint payment_target_school_id_fkey foreign key (target_school_id) references school (id);
update payment set school_id=(SELECT school_id from account_school acs where school_id = acs.school_id limit 1)
