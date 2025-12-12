-- Crear tabla messages para el sistema de mensajes de colaboradores
CREATE TABLE messages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sender_user_id NVARCHAR(255) NOT NULL,
    sender_name NVARCHAR(255),
    sender_email NVARCHAR(255),
    subject NVARCHAR(255) NOT NULL,
    message NTEXT NOT NULL,
    status NVARCHAR(50) NOT NULL DEFAULT 'UNREAD',
    priority NVARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    category NVARCHAR(100),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    read_at DATETIME2,
    replied_at DATETIME2,
    admin_response NTEXT,
    admin_user_id NVARCHAR(255),
    admin_name NVARCHAR(255)
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IX_messages_sender_user_id ON messages(sender_user_id);
CREATE INDEX IX_messages_status ON messages(status);
CREATE INDEX IX_messages_priority ON messages(priority);
CREATE INDEX IX_messages_category ON messages(category);
CREATE INDEX IX_messages_created_at ON messages(created_at);
CREATE INDEX IX_messages_admin_user_id ON messages(admin_user_id);

-- Agregar comentarios a la tabla
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Tabla para almacenar mensajes enviados por colaboradores al administrador', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'messages';

-- Comentarios para las columnas
EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'ID único del mensaje', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'messages', 
    @level2type = N'COLUMN', @level2name = N'id';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'ID del usuario que envía el mensaje', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'messages', 
    @level2type = N'COLUMN', @level2name = N'sender_user_id';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Nombre del usuario remitente', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'messages', 
    @level2type = N'COLUMN', @level2name = N'sender_name';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Email del usuario remitente', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'messages', 
    @level2type = N'COLUMN', @level2name = N'sender_email';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Asunto del mensaje', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'messages', 
    @level2type = N'COLUMN', @level2name = N'subject';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Contenido del mensaje', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'messages', 
    @level2type = N'COLUMN', @level2name = N'message';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Estado del mensaje (UNREAD, READ, REPLIED, ARCHIVED)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'messages', 
    @level2type = N'COLUMN', @level2name = N'status';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Prioridad del mensaje (LOW, NORMAL, HIGH, URGENT)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'messages', 
    @level2type = N'COLUMN', @level2name = N'priority';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'Categoría del mensaje (SUPPORT, COMPLAINT, SUGGESTION, OTHER)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'messages', 
    @level2type = N'COLUMN', @level2name = N'category';

-- Insertar algunos datos de ejemplo para pruebas (opcional)
/*
INSERT INTO messages (sender_user_id, sender_name, sender_email, subject, message, status, priority, category)
VALUES 
    ('user123', 'Juan Pérez', 'juan@email.com', 'Problema con memorial', 'Tengo un problema para acceder a mi memorial colaborativo', 'UNREAD', 'NORMAL', 'SUPPORT'),
    ('user456', 'María García', 'maria@email.com', 'Sugerencia de mejora', 'Me gustaría sugerir una nueva funcionalidad para la app', 'READ', 'LOW', 'SUGGESTION'),
    ('user789', 'Carlos López', 'carlos@email.com', 'Error en la aplicación', 'La aplicación se cierra inesperadamente', 'REPLIED', 'HIGH', 'SUPPORT');
*/

PRINT 'Tabla messages creada exitosamente con índices y comentarios.';