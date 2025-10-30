-- add_atm_reports_table.sql
-- Migration: create table to store user-submitted ATM status reports

CREATE TABLE IF NOT EXISTS atm_reports (
  report_id INT AUTO_INCREMENT PRIMARY KEY,
  atm_id INT NULL,
  place_id VARCHAR(255) NULL,
  reporter VARCHAR(100) NULL,
  reported_up BOOLEAN NULL,
  reported_has_cash BOOLEAN NULL,
  reported_deposit_working BOOLEAN NULL,
  reported_passbook_working BOOLEAN NULL,
  notes VARCHAR(1024) NULL,
  report_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (atm_id) REFERENCES ATM_details(atm_id) ON DELETE SET NULL
);

-- Indexes for faster lookups
CREATE INDEX idx_atm_reports_atm_id ON atm_reports(atm_id);
CREATE INDEX idx_atm_reports_place_id ON atm_reports(place_id);
