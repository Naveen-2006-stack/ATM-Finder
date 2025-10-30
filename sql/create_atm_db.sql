-- Create database
CREATE DATABASE IF NOT EXISTS atm_db
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

USE atm_db;

-- Create ATM_details table
CREATE TABLE IF NOT EXISTS ATM_details (
  atm_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  bank_name VARCHAR(100) NOT NULL,
  location_address VARCHAR(255),
  pincode VARCHAR(10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Cash_availability table
CREATE TABLE IF NOT EXISTS Cash_availability (
  entry_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  atm_id INT NOT NULL,
  available_cash DECIMAL(10,2) NOT NULL,
  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_cash_atm
    FOREIGN KEY (atm_id)
    REFERENCES ATM_details(atm_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert 5 sample rows into ATM_details
INSERT INTO ATM_details (bank_name, location_address, pincode) VALUES
  ('State Bank of Example', '12 MG Road, Chennai', '600001'),
  ('Example National Bank', '45 Anna Salai, Chennai', '600002'),
  ('City Bank', '7 Pondy Bazaar, Chennai', '600003'),
  ('Metro Bank', '100 T. Nagar, Chennai', '600001'),
  ('First Trust Bank', '22 Mount Road, Chennai', '600004');

-- Insert 5 corresponding rows into Cash_availability
-- (Assumes atm_id values are 1..5 in the order inserted above)
INSERT INTO Cash_availability (atm_id, available_cash) VALUES
  (1, 500000.00),  -- very high cash
  (2, 250000.50),  -- high cash with cents
  (3, 5000.00),    -- low cash
  (4, 20000.00),   -- moderate cash
  (5, 100.00);     -- very low cash (almost empty)
