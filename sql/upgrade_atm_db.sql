-- upgrade_atm_db.sql
-- Migration: add has_deposit and has_passbook_printer to ATM_details
-- Run this script against the `atm_db` database.

-- 1) Add the two new boolean columns with default FALSE
ALTER TABLE ATM_details
  ADD COLUMN has_deposit BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN has_passbook_printer BOOLEAN NOT NULL DEFAULT FALSE;

-- 2) Set feature flags for existing sample ATMs (atm_id = 1..5)
UPDATE ATM_details
  SET has_deposit = TRUE, has_passbook_printer = TRUE
  WHERE atm_id = 1;

UPDATE ATM_details
  SET has_deposit = TRUE, has_passbook_printer = FALSE
  WHERE atm_id = 2;

UPDATE ATM_details
  SET has_deposit = FALSE, has_passbook_printer = TRUE
  WHERE atm_id = 3;

UPDATE ATM_details
  SET has_deposit = FALSE, has_passbook_printer = FALSE
  WHERE atm_id IN (4,5);

-- 3) Simple verification selects
SELECT atm_id, bank_name, location_address, has_deposit, has_passbook_printer
  FROM ATM_details
  WHERE atm_id BETWEEN 1 AND 5
  ORDER BY atm_id;
