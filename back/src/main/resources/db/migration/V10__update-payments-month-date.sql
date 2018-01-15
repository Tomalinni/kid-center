alter TABLE category add column hastargetmonth BOOLEAN DEFAULT false;
UPDATE payment SET monthdate = date_trunc('month', date) where monthdate is null and date notnull;