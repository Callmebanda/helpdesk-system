CREATE INDEX idx_tickets_user_id ON tickets(user_id);

CREATE INDEX idx_tickets_assigned_technician_id ON tickets(assigned_technician_id);

CREATE INDEX idx_tickets_status ON tickets(status);

CREATE INDEX idx_tickets_priority ON tickets(priority);

CREATE INDEX idx_tickets_device_type ON tickets(device_type);

CREATE INDEX idx_tickets_issue_category ON tickets(issue_category);

CREATE INDEX idx_tickets_created_at ON tickets(created_at);

CREATE INDEX idx_tickets_due_at ON tickets(due_at);

CREATE INDEX idx_devices_assigned_user_id ON devices(assigned_user_id);

CREATE INDEX idx_devices_status ON devices(status);

CREATE INDEX idx_devices_device_type ON devices(device_type);

CREATE INDEX idx_device_reports_reported_by_user_id ON device_reports(reported_by_user_id);

CREATE INDEX idx_device_reports_status ON device_reports(status);

CREATE INDEX idx_device_reports_created_at ON device_reports(created_at);

CREATE INDEX idx_knowledge_articles_device_type ON knowledge_articles(device_type);

CREATE INDEX idx_knowledge_articles_issue_category ON knowledge_articles(issue_category);

CREATE INDEX idx_knowledge_articles_created_at ON knowledge_articles(created_at);

CREATE INDEX idx_ticket_activities_ticket_id ON ticket_activities(ticket_id);

CREATE INDEX idx_ticket_activities_created_at ON ticket_activities(created_at);