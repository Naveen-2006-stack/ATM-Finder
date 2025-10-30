-- gemini_restructure_atm_reports.sql
-- Migration: replace ATM_reports with Gemini-oriented schema

-- Drop existing table if present
DROP TABLE IF EXISTS ATM_reports;

-- Create new ATM_reports tailored to Gemini-provided data
CREATE TABLE ATM_reports (
  report_id INT AUTO_INCREMENT PRIMARY KEY,
  atm_name VARCHAR(255) NOT NULL,
  atm_address VARCHAR(500) NOT NULL,
  cash_status VARCHAR(50),
  deposit_status VARCHAR(50),
  passbook_status VARCHAR(50),
  report_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index to speed up lookups by name and address
CREATE INDEX idx_atm_location ON ATM_reports (atm_name, atm_address);

-- Sample data for testing
INSERT INTO ATM_reports (atm_name, atm_address, cash_status)
  VALUES ('Test SBI ATM', '123 Test Street, 600001', 'OUT_OF_CASH');
