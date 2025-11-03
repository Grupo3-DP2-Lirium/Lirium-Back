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
    
    /**
     * Envía un código de recuperación de contraseña por email.
     * Si SendGrid está deshabilitado (dev), solo logea en consola.
     * Si está habilitado (prod), envía email real via SendGrid.
     * 
     * @param toEmail Email del destinatario
     * @param code Código de 6 dígitos para recuperación
     */
    public void sendPasswordResetCode(String toEmail, String code) {
        if (!sendGridEnabled) {
            // Modo desarrollo: solo logear
            logEmailToConsole(toEmail, code);
            return;
        }
        
        try {
            // Modo producción: enviar email real
            sendRealEmail(toEmail, code);
            logger.info("✅ Email de recuperación enviado exitosamente a: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Error al enviar email a {}: {}", toEmail, e.getMessage(), e);
            // En producción, podrías querer lanzar una excepción personalizada
            // throw new EmailSendException("Error al enviar email de recuperación", e);
        }
    }
    
    /**
     * Envía el email real usando SendGrid API
     */
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
        
        // SendGrid retorna 202 cuando el email es aceptado para envío
        if (response.getStatusCode() >= 400) {
            logger.error("SendGrid error - Status: {}, Body: {}", 
                response.getStatusCode(), response.getBody());
            throw new IOException("SendGrid API error: " + response.getBody());
        }
        
        logger.debug("SendGrid response - Status: {}, Headers: {}", 
            response.getStatusCode(), response.getHeaders());
    }
    
    /**
     * Logea el email en consola (modo desarrollo)
     */
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
    
    /**
     * Construye el cuerpo del email
     */
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
