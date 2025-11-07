package org.example.springboot_backend.enums;

public enum AuditAction {
    // User actions
    USER_LOGIN,
    USER_LOGOUT,
    USER_REGISTER,
    USER_UPDATE,
    USER_DELETE,
    USER_DISABLE,
    USER_ENABLE,
    
    // Memorial actions
    MEMORIAL_CREATE,
    MEMORIAL_UPDATE,
    MEMORIAL_DELETE,
    MEMORIAL_SHARE,
    
    // Memory actions
    MEMORY_CREATE,
    MEMORY_UPDATE,
    MEMORY_DELETE,
    MEMORY_UPLOAD,
    
    // Subscription actions
    SUBSCRIPTION_CREATE,
    SUBSCRIPTION_UPDATE,
    SUBSCRIPTION_CANCEL,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    
    // Admin actions
    ADMIN_USER_DISABLE,
    ADMIN_USER_ENABLE,
    ADMIN_VIEW_LOGS,
    
    // System actions
    SYSTEM_ERROR,
    SYSTEM_WARNING
}
