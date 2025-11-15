-- Create events table
CREATE TABLE IF NOT EXISTS events (
    id VARCHAR(255) PRIMARY KEY,
    calendar_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    disabled BOOLEAN DEFAULT FALSE,
    all_day BOOLEAN DEFAULT FALSE,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    location VARCHAR(255),
    created_at VARCHAR(50),
    created_by VARCHAR(255),
    updated_at VARCHAR(50),
    updated_by VARCHAR(255),
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_events_calendar FOREIGN KEY (calendar_id) REFERENCES calendars(id) ON DELETE CASCADE
);

-- Create index on calendar_id for efficient lookups
CREATE INDEX idx_events_calendar_id ON events(calendar_id);

-- Create index on start_datetime for date range queries
CREATE INDEX idx_events_start_datetime ON events(start_datetime);

-- Create index on type for filtering
CREATE INDEX idx_events_type ON events(type);

