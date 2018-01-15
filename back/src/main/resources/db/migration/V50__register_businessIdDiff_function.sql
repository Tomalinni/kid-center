CREATE OR REPLACE FUNCTION businessIdDiff(
  IN id1 TEXT,
  IN id2 TEXT)
  RETURNS INTEGER AS $$
BEGIN
  RETURN abs(to_number(id1, '9999') - to_number(id2, '9999'));
END;
$$ LANGUAGE plpgsql;