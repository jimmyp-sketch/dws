# Dws_demo_task


#DB SCRIPT For create table 

-- Create the accounts table
CREATE TABLE accounts (
    id bigint PRIMARY KEY,       -- Unique identifier for each account
    account_holder_name VARCHAR(100) NOT NULL,  -- Account holder's name
    balance NUMERIC(15, 2) NOT NULL CHECK (balance >= 0),  -- Account balance with no overdraft allowed
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP  -- Timestamp of account creation
);

-- Create the transfers table (optional for tracking transfer history)
CREATE TABLE transfers (
    id BIGSERIAL PRIMARY KEY,              -- Unique identifier for each transfer (auto-incrementing)
    account_from BIGINT REFERENCES accounts(id) ON DELETE CASCADE,  -- Foreign key to the sender's account
    account_to BIGINT REFERENCES accounts(id) ON DELETE CASCADE,    -- Foreign key to the receiver's account
    amount NUMERIC(15, 2) NOT NULL CHECK (amount > 0),  -- Amount to be transferred (must be positive)
    transfer_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP  -- Timestamp of the transfer
);


-- Indexes (optional but improves performance for search queries)
CREATE INDEX idx_account_from ON transfers (account_from);
CREATE INDEX idx_account_to ON transfers (account_to);

-- Sample data insertion for testing
INSERT INTO accounts (id,account_holder_name, balance) VALUES (1001,'John Doe', 1000.00);
INSERT INTO accounts (id,account_holder_name, balance) VALUES (1002,'Jane Smith', 500.00);

# CURL command for the post request call from the endpoint 

curl --location 'http://localhost:9091/api/v1/transfers' \
--header 'Content-Type: application/json' \
--data '{
    "accountFromId":1001,
    "accountToId":1002,
    "amount": 200.00
}'
