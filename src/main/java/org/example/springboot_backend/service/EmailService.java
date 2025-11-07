package org.example.springboot_backend.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final SendGrid sendGridClient;
    private final String fromEmail;
    private final String fromName;
    private final boolean sendGridEnabled;
    
    public EmailService(
            @Value("${sendgrid.api.key}") String apiKey,
            @Value("${sendgrid.from.email}") String fromEmail,
            @Value("${sendgrid.from.name}") String fromName,
            @Value("${sendgrid.enabled:true}") boolean sendGridEnabled) {
        this.sendGridClient = new SendGrid(apiKey);
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.sendGridEnabled = sendGridEnabled;
        
        logger.info("EmailService initialized - SendGrid enabled: {}", sendGridEnabled);
        logger.info("From email configured: {}", fromEmail);
    }
    
    // ========== NUEVO: Invitación directa por email ==========
    
    /**
     * Envía invitación directa a un email específico.
     * Crea automáticamente el código y lo envía.
     * 
     * @param toEmail Email del usuario a invitar
     * @param inviterName Nombre del que invita
     * @param memorialName Nombre del memorial
     * @param inviteCode Código de invitación generado
     * @param canEdit Si puede editar
     * @param canComment Si puede comentar
     */
    public void sendMemorialInvitation(
            String toEmail, 
            String inviterName,
            String memorialName,
            String inviteCode,
            boolean canEdit,
            boolean canComment) {
        
        if (!sendGridEnabled) {
            logInvitationToConsole(toEmail, inviterName, memorialName, inviteCode, canEdit, canComment);
            return;
        }
        
        try {
            sendInvitationEmail(toEmail, inviterName, memorialName, inviteCode, canEdit, canComment);
            logger.info("✅ Invitación enviada exitosamente a: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Error al enviar invitación a {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Error al enviar invitación por email", e);
        }
    }
    
    private void sendInvitationEmail(
            String toEmail,
            String inviterName,
            String memorialName,
            String inviteCode,
            boolean canEdit,
            boolean canComment) throws IOException {
        
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = String.format("%s te invitó a colaborar en Remory", inviterName);
        Content content = new Content("text/html", buildInvitationEmailBody(
            inviterName, memorialName, inviteCode, canEdit, canComment
        ));
        
        Mail mail = new Mail(from, subject, to, content);
        
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        
        Response response = sendGridClient.api(request);
        
        if (response.getStatusCode() >= 400) {
            logger.error("SendGrid error - Status: {}, Body: {}", 
                response.getStatusCode(), response.getBody());
            throw new IOException("SendGrid API error: " + response.getBody());
        }
    }
    
    private String buildInvitationEmailBody(
            String inviterName,
            String memorialName,
            String inviteCode,
            boolean canEdit,
            boolean canComment) {
        
        String permissions = canEdit ? "editar y comentar" : 
                           canComment ? "comentar" : "ver";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                              color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .code-box { background: white; border: 2px dashed #667eea; 
                               padding: 20px; margin: 20px 0; text-align: center; border-radius: 8px; }
                    .code { font-size: 32px; font-weight: bold; letter-spacing: 8px; 
                           color: #667eea; font-family: monospace; }
                    .button { display: inline-block; background: #667eea; color: white; 
                             padding: 12px 30px; text-decoration: none; border-radius: 5px; 
                             margin: 20px 0; font-weight: bold; }
                    .permissions { background: #e8f4f8; padding: 15px; border-radius: 5px; 
                                  margin: 15px 0; border-left: 4px solid #667eea; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎊 ¡Tienes una invitación!</h1>
                    </div>
                    <div class="content">
                        <p>Hola,</p>
                        <p><strong>%s</strong> te ha invitado a colaborar en el memorial 
                           <strong>"%s"</strong> en Remory.</p>
                        
                        <div class="permissions">
                            <strong>📋 Tus permisos:</strong><br>
                            Podrás <strong>%s</strong> en este memorial.
                        </div>
                        
                        <p>Para aceptar la invitación, sigue estos pasos:</p>
                        <ol>
                            <li>Abre la app Remory</li>
                            <li>Ve a "Colaboraciones"</li>
                            <li>Toca "Ingresar código"</li>
                            <li>Ingresa el siguiente código:</li>
                        </ol>
                        
                        <div class="code-box">
                            <div class="code">%s</div>
                            <p style="color: #666; font-size: 12px; margin-top: 10px;">
                                ⏰ Válido por 24 horas
                            </p>
                        </div>
                        
                        <p style="color: #666; font-size: 14px;">
                            💡 <strong>Tip:</strong> Si aún no tienes Remory, puedes descargarla 
                            desde tu tienda de aplicaciones favorita.
                        </p>
                        
                        <p>Si no esperabas esta invitación, puedes ignorar este correo de forma segura.</p>
                        
                        <p>¡Esperamos que disfrutes colaborando en Remory!</p>
                        
                        <div class="footer">
                            <p>Este es un correo automático, por favor no respondas.<br>
                            © 2024 Remory - Preservando memorias, construyendo legados</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, inviterName, memorialName, permissions, inviteCode);
    }
    
    private void logInvitationToConsole(
            String toEmail,
            String inviterName,
            String memorialName,
            String inviteCode,
            boolean canEdit,
            boolean canComment) {
        
        logger.info("============================================");
        logger.info("📧 INVITACIÓN SIMULADA (Modo Desarrollo)");
        logger.info("============================================");
        logger.info("Para: {}", toEmail);
        logger.info("De: {} <{}>", fromName, fromEmail);
        logger.info("Asunto: {} te invitó a colaborar en Remory", inviterName);
        logger.info("");
        logger.info("Invitado por: {}", inviterName);
        logger.info("Memorial: {}", memorialName);
        logger.info("Código: {}", inviteCode);
        logger.info("Permisos: Editar={}, Comentar={}", canEdit, canComment);
        logger.info("============================================");
        logger.info("✅ Invitación simulada registrada en logs");
        logger.info("============================================");
    }
    
    // ========== Método original (sin cambios) ==========
    
    public void sendPasswordResetCode(String toEmail, String code) {
        if (!sendGridEnabled) {
            logEmailToConsole(toEmail, code);
            return;
        }
        
        try {
            sendRealEmail(toEmail, code);
            logger.info("✅ Email de recuperación enviado exitosamente a: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Error al enviar email a {}: {}", toEmail, e.getMessage(), e);
        }
    }
    
    private void sendRealEmail(String toEmail, String code) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "Código de recuperación - Remory";
        Content content = new Content("text/plain", buildEmailBody(code));
        
        Mail mail = new Mail(from, subject, to, content);
        
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        
        Response response = sendGridClient.api(request);
        
        if (response.getStatusCode() >= 400) {
            logger.error("SendGrid error - Status: {}, Body: {}", 
                response.getStatusCode(), response.getBody());
            throw new IOException("SendGrid API error: " + response.getBody());
        }
        
        logger.debug("SendGrid response - Status: {}, Headers: {}", 
            response.getStatusCode(), response.getHeaders());
    }
    
    private void logEmailToConsole(String toEmail, String code) {
        logger.info("============================================");
        logger.info("📧 EMAIL SIMULADO (Modo Desarrollo)");
        logger.info("============================================");
        logger.info("Para: {}", toEmail);
        logger.info("De: {} <{}>", fromName, fromEmail);
        logger.info("Asunto: Código de recuperación - Remory");
        logger.info("");
        logger.info("Contenido del mensaje:");
        logger.info("---");
        logger.info(buildEmailBody(code));
        logger.info("---");
        logger.info("============================================");
        logger.info("✅ Email simulado registrado en logs");
        logger.info("============================================");
    }
    
    private String buildEmailBody(String code) {
        return String.format("""
            Hola,
            
            Recibimos una solicitud para restablecer tu contraseña en Remory.
            
            Tu código de verificación es: %s
            
            Este código expirará en 10 minutos.
            
            Si no solicitaste este cambio, puedes ignorar este correo de forma segura.
            
            Saludos,
            Equipo Remory
            """, code);
    }
}