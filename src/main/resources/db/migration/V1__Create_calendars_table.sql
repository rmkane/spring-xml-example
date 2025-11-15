-- Create calendars table
CREATE TABLE IF NOT EXISTS calendars (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'unknown',
    visibility VARCHAR(50) NOT NULL DEFAULT 'personal',
    created_at VARCHAR(50),
    created_by VARCHAR(255),
    updated_at VARCHAR(50),
    updated_by VARCHAR(255),
    count INTEGER DEFAULT 0,
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on status for filtering
CREATE INDEX idx_calendars_status ON calendars(status);

-- Create index on visibility for filtering
CREATE INDEX idx_calendars_visibility ON calendars(visibility);

