create table account_school (
  account_id bigint,
  school_id bigint,
  constraint acc_school_acc_id_fkey foreign key (account_id) references account (id),
  constraint acc_school_school_id_fkey foreign key (school_id) references school (id)
);

with accs as ( select id, school_id from account )
insert into account_school select id, school_id from accs;
