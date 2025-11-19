-- Crear tabla de audit logs
CREATE TABLE audit_logs (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    action NVARCHAR(255) NOT NULL,
    user_email NVARCHAR(255) NOT NULL,
    details NVARCHAR(1000),
    timestamp DATETIME2 NOT NULL DEFAULT GETDATE(),
    ip_address NVARCHAR(50)
);

-- Crear índices para mejorar el rendimiento de las consultas
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_user_email ON audit_logs(user_email);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
