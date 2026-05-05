BEGIN;

ALTER TABLE restaurants
    ADD COLUMN IF NOT EXISTS primary_category_name VARCHAR(50),
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS business_hours_raw TEXT;

UPDATE restaurants
SET business_hours_raw = opening_hours
WHERE business_hours_raw IS NULL
  AND opening_hours IS NOT NULL
  AND BTRIM(opening_hours) <> '';

ALTER TABLE restaurants
    DROP COLUMN IF EXISTS opening_hours,
    DROP COLUMN IF EXISTS business_hours_summary;

UPDATE restaurants
SET primary_category_name = CASE
    WHEN category_name IS NULL OR BTRIM(category_name) = '' THEN NULL
    WHEN category_name ILIKE U&'%\C911\C2DD%'
        OR category_name ILIKE U&'%\C9DC\C7A5%'
        OR category_name ILIKE U&'%\C9EC\BF55%'
        OR category_name ILIKE U&'%\B9C8\B77C%'
        OR category_name ILIKE U&'%\C591\AF2C\CE58%' THEN U&'\C911\C2DD'
    WHEN category_name ILIKE U&'%\C77C\C2DD%'
        OR category_name ILIKE U&'%\CD08\BC25%'
        OR category_name ILIKE U&'%\C2A4\C2DC%'
        OR category_name ILIKE U&'%\B77C\BA58%'
        OR category_name ILIKE U&'%\C6B0\B3D9%'
        OR category_name ILIKE U&'%\C18C\BC14%'
        OR category_name ILIKE U&'%\B3C8\AC00\C2A4%'
        OR category_name ILIKE U&'%\B3C8\AE4C\C2A4%'
        OR category_name ILIKE U&'%\CE74\CE20%'
        OR category_name ILIKE U&'%\C0DD\C120\D68C%'
        OR category_name ILIKE U&'%\C774\C790\CE74\C57C%' THEN U&'\C77C\C2DD'
    WHEN category_name ILIKE U&'%\C591\C2DD%'
        OR category_name ILIKE U&'%\D30C\C2A4\D0C0%'
        OR category_name ILIKE U&'%\C2A4\D30C\AC8C\D2F0%'
        OR category_name ILIKE U&'%\C2A4\D14C\C774\D06C%'
        OR category_name ILIKE U&'%\D53C\C790%'
        OR category_name ILIKE U&'%\D584\BC84\AC70%'
        OR category_name ILIKE U&'%\BC84\AC70%'
        OR category_name ILIKE U&'%\C0CC\B4DC\C704\CE58%'
        OR category_name ILIKE U&'%\BE0C\B7F0\CE58%'
        OR category_name ILIKE U&'%\D0C0\CF54%'
        OR category_name ILIKE U&'%\BA55\C2DC\CF54%' THEN U&'\C591\C2DD'
    WHEN category_name ILIKE U&'%\BD84\C2DD%'
        OR category_name ILIKE U&'%\AE40\BC25%'
        OR category_name ILIKE U&'%\B5A1\BCF6\C774%'
        OR category_name ILIKE U&'%\C21C\B300%'
        OR category_name ILIKE U&'%\D280\AE40%' THEN U&'\BD84\C2DD'
    WHEN category_name ILIKE U&'%\CE74\D398%'
        OR category_name ILIKE U&'%\CEE4\D53C%'
        OR category_name ILIKE U&'%\B514\C800\D2B8%'
        OR category_name ILIKE U&'%\BCA0\C774\CEE4\B9AC%'
        OR category_name ILIKE U&'%\BE75%'
        OR category_name ILIKE U&'%\CF00\C774\D06C%'
        OR category_name ILIKE U&'%\B3C4\B11B%' THEN U&'\CE74\D398/\B514\C800\D2B8'
    WHEN category_name ILIKE U&'%\CE58\D0A8%'
        OR category_name ILIKE U&'%\B2ED\AC15\C815%' THEN U&'\CE58\D0A8'
    WHEN category_name ILIKE U&'%\C220\C9D1%'
        OR category_name ILIKE U&'%\C8FC\C810%'
        OR category_name ILIKE U&'%\D638\D504%'
        OR category_name ILIKE U&'%\B9E5\C8FC%' THEN U&'\C220\C9D1'
    WHEN category_name ILIKE U&'%\D55C\C2DD%'
        OR category_name ILIKE U&'%\AD6D\BC25%'
        OR category_name ILIKE U&'%\CC0C\AC1C%'
        OR category_name ILIKE U&'%\BC25%'
        OR category_name ILIKE U&'%\D574\C7A5\AD6D%'
        OR category_name ILIKE U&'%\ACE0\AE30%'
        OR category_name ILIKE U&'%\AD6C\C774%'
        OR category_name ILIKE U&'%\AC08\BE44%'
        OR category_name ILIKE U&'%\C0BC\ACB9\C0B4%'
        OR category_name ILIKE U&'%\BCF4\C30C%'
        OR category_name ILIKE U&'%\C871\BC1C%'
        OR category_name ILIKE U&'%\B0C9\BA74%'
        OR category_name ILIKE U&'%\CE7C\AD6D\C218%'
        OR category_name ILIKE U&'%\BC31\BC18%'
        OR category_name ILIKE U&'%\C694\B9AC%'
        OR category_name ILIKE U&'%\B5A1\AC08\BE44%' THEN U&'\D55C\C2DD'
    ELSE U&'\AE30\D0C0'
END
WHERE primary_category_name IS NULL;

COMMIT;
