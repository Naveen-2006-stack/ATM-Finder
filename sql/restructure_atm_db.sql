-- restructure_atm_db.sql
-- Drop legacy ATM tables and create new ATM_reports table for user-submitted reports

-- Drop old tables if they exist
DROP TABLE IF EXISTS Cash_availability;
DROP TABLE IF EXISTS ATM_details;

-- Create new ATM_reports table
CREATE TABLE IF NOT EXISTS ATM_reports (
  report_id INT AUTO_INCREMENT PRIMARY KEY,
  google_place_id VARCHAR(255) NOT NULL,
  atm_name VARCHAR(255),
  cash_status VARCHAR(50),
  deposit_status VARCHAR(50),
  passbook_status VARCHAR(50),
  report_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index for faster lookup by Google Place ID
CREATE INDEX idx_place_id ON ATM_reports (google_place_id);

-- Sample data for testing
INSERT INTO ATM_reports (google_place_id, atm_name, cash_status)
  VALUES ('some_fake_google_id_123', 'Sample SBI ATM', 'OUT_OF_CASH');

INSERT INTO ATM_reports (google_place_id, atm_name, cash_status, deposit_status, passbook_status)
  VALUES ('fake_google_456', 'Sample HDFC ATM', 'WORKING', 'WORKING', 'NOT_AVAILABLE');
-- restructure_atm_db.sql
-- Creates a new ATM_reports table for user-submitted reports (non-destructive).
-- NOTE: This migration will NOT drop existing `ATM_details` or `Cash_availability` tables; it only adds `ATM_reports`.

-- 1) Create new reports table
CREATE TABLE ATM_reports (
  report_id INT PRIMARY KEY AUTO_INCREMENT,
  google_place_id VARCHAR(255) NOT NULL,
  atm_name VARCHAR(255),
  cash_status VARCHAR(50),
  deposit_status VARCHAR(50),
  passbook_status VARCHAR(50),
  report_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) Index to speed up lookups by place id
CREATE INDEX idx_place_id ON ATM_reports (google_place_id);

-- 4) Sample data for testing
INSERT INTO ATM_reports (google_place_id, atm_name, cash_status, deposit_status, passbook_status)
VALUES ('some_fake_google_id_123', 'Sample SBI ATM', 'OUT_OF_CASH', 'WORKING', 'NOT_AVAILABLE');

INSERT INTO ATM_reports (google_place_id, atm_name, cash_status, deposit_status, passbook_status)
VALUES ('another_fake_google_id_456', 'Sample HDFC ATM', 'WORKING', 'BROKEN', 'WORKING');

-- 5) Verification select
SELECT report_id, google_place_id, atm_name, cash_status, deposit_status, passbook_status, report_timestamp
  FROM ATM_reports
  ORDER BY report_timestamp DESC
  LIMIT 10;
