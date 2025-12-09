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
        String subject = String.format("%s te invitó a colaborar en Lirium", inviterName);
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

    String permissions = canEdit ? "editar contenido y comentar" :
                        canComment ? "comentar" : "ver contenido";

    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background: #f5f7fa; }
                .container { max-width: 620px; margin: 0 auto; padding: 20px; }
                .header { background: #FC7171;
                          color: white; padding: 32px; text-align: center;
                          border-radius: 14px 14px 0 0; }
                .content { background: #ffffff; padding: 32px; border-radius: 0 0 14px 14px;
                           box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
                .code-box { background: #fafafa; border: 2px dashed #4F46E5;
                           padding: 22px; margin: 24px 0; text-align: center; border-radius: 10px; }
                .code { font-size: 34px; font-weight: bold; letter-spacing: 8px;
                       color: #4F46E5; font-family: monospace; }
                .permissions { background: #eef2ff; padding: 18px; border-radius: 8px;
                              margin: 20px 0; border-left: 5px solid #4F46E5; }
                .footer { text-align: center; color: #888; font-size: 12px; margin-top: 30px; }
                .register-box { background: #fff8e1; border: 1px solid #ffecb5;
                               padding: 16px; border-radius: 10px; margin: 18px 0; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>🌟 ¡Tienes una invitación a Lirium!</h1>
                </div>

                <div class="content">
                    <p>Hola,</p>

                    <p><strong>%s</strong> te ha invitado a unirte a un memorial llamado 
                    <strong>"%s"</strong>, un espacio especial dentro de <strong>Lirium</strong> para honrar y preservar recuerdos significativos.</p>

                    <div class="permissions">
                        <strong>🔐 Tus permisos:</strong><br>
                        Podrás <strong>%s</strong> dentro de este memorial.
                    </div>

                    <div class="register-box">
                        <strong>⚠️ ¿Primera vez en Lirium?</strong><br>
                        Si no tienes cuenta, deberás crear una antes de ingresar el código.
                    </div>

                    <p>Para unirte, sigue estos pasos:</p>
                    <ol>
                        <li>Abre la app <strong>Lirium</strong> en tu dispositivo móvil.</li>
                        <li>Inicia sesión o crea una cuenta si aún no tienes una.</li>
                        <li>Ve a <strong>“Memoriales”</strong> en la parte inferior.</li>
                        <li>Ingresa a <strong>“Colaboraciones”</strong>.</li>
                        <li>Toca <strong>“Ingresar código”</strong>.</li>
                        <li>Introduce el siguiente código:</li>
                    </ol>

                    <div class="code-box">
                        <div class="code">%s</div>
                        <p style="color: #777; font-size: 12px; margin-top: 10px;">
                            ⏰ Válido por 24 horas.
                        </p>
                    </div>

                    <p style="color: #666; font-size: 14px;">
                        💡 <strong>Tip:</strong> Guarda este código para facilitar tu ingreso cuando abras la app.
                    </p>

                    <p>Si no esperabas esta invitación, puedes ignorar este mensaje de manera segura.</p>

                    <p>Gracias por ser parte de Lirium ✨</p>

                    <div class="footer">
                        <p>Este es un correo automático, por favor no respondas.<br>
                        © 2025 Lirium</p>
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
        String subject = "Código de recuperación - Lirium";
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
            
            Recibimos una solicitud para restablecer tu contraseña en Lirium.
            
            Tu código de verificación es: %s
            
            Este código expirará en 10 minutos.
            
            Si no solicitaste este cambio, puedes ignorar este correo de forma segura.
            
            Saludos,
            Equipo Lirium
            """, code);
    }
}