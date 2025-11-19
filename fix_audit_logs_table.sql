-- Script para corregir la tabla audit_logs existente
-- Agregar las columnas faltantes con DEFAULT para permitir agregar a tabla no vacía

-- Agregar columna admin_id si no existe (con DEFAULT)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('audit_logs') AND name = 'admin_id')
BEGIN
    ALTER TABLE audit_logs ADD admin_id NVARCHAR(255) NOT NULL DEFAULT 'admin@gmail.com';
    PRINT 'Columna admin_id agregada';
END
ELSE
BEGIN
    PRINT 'Columna admin_id ya existe';
END

-- Agregar columna admin_email si no existe (con DEFAULT)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('audit_logs') AND name = 'admin_email')
BEGIN
    ALTER TABLE audit_logs ADD admin_email NVARCHAR(255) NOT NULL DEFAULT 'admin@gmail.com';
    PRINT 'Columna admin_email agregada';
END
ELSE
BEGIN
    PRINT 'Columna admin_email ya existe';
END

-- Agregar columna timestamp si no existe (con DEFAULT)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('audit_logs') AND name = 'timestamp')
BEGIN
    ALTER TABLE audit_logs ADD timestamp DATETIME2 NOT NULL DEFAULT GETDATE();
    PRINT 'Columna timestamp agregada';
END
ELSE
BEGIN
    PRINT 'Columna timestamp ya existe';
END

PRINT 'Tabla audit_logs actualizada correctamente';

-- Verificar la estructura
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'audit_logs'
ORDER BY ORDINAL_POSITION;
