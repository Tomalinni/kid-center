UPDATE studentcard
SET activationdate = purchasedate;

ALTER TABLE studentcard
  ALTER COLUMN activationdate SET DEFAULT ('now' :: TEXT) :: DATE;
ALTER TABLE studentcard
  ALTER COLUMN activationdate SET NOT NULL;