-- Tabla de logs de auditoría
CREATE TABLE audit_logs (
    id_audit_log UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    action VARCHAR(100) NOT NULL,
    user_email VARCHAR(255),
    user_id UNIQUEIDENTIFIER,
    ip_address VARCHAR(45),
    entity_type VARCHAR(100),
    entity_id VARCHAR(255),
    details TEXT,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    success BIT DEFAULT 1,
    error_message TEXT
);

-- Índices para mejorar el rendimiento de las consultas
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_user_email ON audit_logs(user_email);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

-- Comentarios
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Tabla de auditoría para registrar todas las operaciones administrativas del sistema',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'audit_logs';
