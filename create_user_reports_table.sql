-- Crear tabla user_reports para el sistema de reportes de usuarios
CREATE TABLE user_reports (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    reporter_user_id NVARCHAR(255) NOT NULL,
    reported_user_id NVARCHAR(255) NOT NULL,
    reason NVARCHAR(255) NOT NULL,
    description NTEXT,
    content_type NVARCHAR(100),
    content_id NVARCHAR(255),
    status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2,
    admin_notes NTEXT,
    resolved_by NVARCHAR(255),
    resolved_at DATETIME2
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IX_user_reports_reporter_user_id ON user_reports(reporter_user_id);
CREATE INDEX IX_user_reports_reported_user_id ON user_reports(reported_user_id);
CREATE INDEX IX_user_reports_status ON user_reports(status);
CREATE INDEX IX_user_reports_created_at ON user_reports(created_at);
CREATE INDEX IX_user_reports_content_type ON user_reports(content_type);

-- Agregar comentarios a la tabla
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Tabla para almacenar reportes de usuarios realizados por colaboradores', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'user_reports';

-- Comentarios para las columnas
EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'ID único del reporte', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'user_reports', 
    @level2type = N'COLUMN', @level2name = N'id';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'ID del usuario que hace el reporte', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'user_reports', 
    @level2type = N'COLUMN', @level2name = N'reporter_user_id';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'ID del usuario reportado', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'user_reports', 
    @level2type = N'COLUMN', @level2name = N'reported_user_id';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Razón del reporte (INAPPROPRIATE_CONTENT, HARASSMENT, etc.)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'user_reports', 
    @level2type = N'COLUMN', @level2name = N'reason';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Descripción detallada del reporte', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'user_reports', 
    @level2type = N'COLUMN', @level2name = N'description';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Tipo de contenido reportado (MEMORY, MEMORIAL, COMMENT, etc.)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'user_reports', 
    @level2type = N'COLUMN', @level2name = N'content_type';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'ID del contenido específico reportado', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'user_reports', 
    @level2type = N'COLUMN', @level2name = N'content_id';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Estado del reporte (PENDING, RESOLVED, DISMISSED)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'user_reports', 
    @level2type = N'COLUMN', @level2name = N'status';

-- Insertar algunos datos de ejemplo para pruebas (opcional)
/*
INSERT INTO user_reports (reporter_user_id, reported_user_id, reason, description, content_type, content_id, status)
VALUES 
    ('user123', 'user456', 'INAPPROPRIATE_CONTENT', 'El usuario subió contenido inapropiado en el memorial', 'MEMORIAL', 'memorial789', 'PENDING'),
    ('user789', 'user456', 'HARASSMENT', 'El usuario está acosando a otros colaboradores', 'COMMENT', 'comment123', 'PENDING'),
    ('user111', 'user222', 'SPAM', 'El usuario está enviando spam constantemente', 'MEMORY', 'memory456', 'RESOLVED');
*/

PRINT 'Tabla user_reports creada exitosamente con índices y comentarios.';