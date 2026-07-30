CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY,
    donor_id UUID NOT NULL UNIQUE REFERENCES donors(id) ON DELETE CASCADE,
    sms_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    silent_start_time TIME,
    silent_end_time TIME,
    sms_opted_out BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE app_notifications (
    id UUID PRIMARY KEY,
    donor_id UUID NOT NULL REFERENCES donors(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_entity_id VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);
