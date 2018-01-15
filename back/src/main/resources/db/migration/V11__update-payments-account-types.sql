alter table account add column accounttype integer default 0;
alter table account add column internal bool default true;
alter table account add column login text;

alter table payment add column target_account_id bigint;
alter table payment add constraint payment_target_account_id_fkey foreign key (target_account_id) references account (id);

alter table school add column haslessons bool default true;