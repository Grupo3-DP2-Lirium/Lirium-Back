package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.*;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.enums.InviteCodeStatusEnum;
import org.example.springboot_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollaboratorService implements ICollaboratorService {

    @Autowired
    private CollaboratorRepository collaboratorRepository;
    
    @Autowired
    private InviteCodeRepository inviteCodeRepository;
    
    @Autowired
    private MemorialRepository memorialRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
private EmailService emailService;

    // ========== ACEPTAR CÓDIGO (CON NOTIFICACIONES) ==========
    
    @Override
    public CollaboratorResponse acceptInviteCode(String code, User user) {
        try {
            System.out.println("✋ Usuario " + user.getEmail() + " acepta código: " + code);
            
            InviteCode inviteCode = inviteCodeRepository.findByCode(code.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Código inválido"));
            
            if (inviteCode.getStatus() != InviteCodeStatusEnum.ACTIVE) {
                throw new RuntimeException("Código no disponible");
            }
            
            if (LocalDateTime.now().isAfter(inviteCode.getExpiresAt())) {
                inviteCode.setStatus(InviteCodeStatusEnum.EXPIRED);
                inviteCodeRepository.save(inviteCode);
                throw new RuntimeException("Código expirado");
            }
            
            if (inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
                inviteCode.setStatus(InviteCodeStatusEnum.USED);
                inviteCodeRepository.save(inviteCode);
                throw new RuntimeException("Código agotado");
            }
            
            Memorial memorial = inviteCode.getMemorial();
            User memorialOwner = memorial.getUser();
            
            if (memorialOwner.getIdUser().equals(user.getIdUser())) {
                throw new RuntimeException("No puedes colaborar en tu propio memorial");
            }
            
            if (collaboratorRepository.existsByUserAndMemorialAndIsActiveTrue(user, memorial)) {
                throw new RuntimeException("Ya eres colaborador");
            }
            
            // Crear colaborador
            Collaborator collaborator = new Collaborator();
            collaborator.setUser(user);
            collaborator.setMemorial(memorial);
            collaborator.setCanEdit(inviteCode.getCanEdit());
            collaborator.setCanComment(inviteCode.getCanComment());
            collaborator.setJoinedDate(LocalDateTime.now());
            collaborator.setIsActive(true);
            
            collaborator = collaboratorRepository.save(collaborator);
            
            // Actualizar código
            inviteCode.setUsedCount(inviteCode.getUsedCount() + 1);
            inviteCode.setUsedAt(LocalDateTime.now());
            
            if (inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
                inviteCode.setStatus(InviteCodeStatusEnum.USED);
            }
            
            inviteCodeRepository.save(inviteCode);
            
            // ✅ NOTIFICACIONES
            try {
                // Notificar al dueño del memorial
                notificationService.notifyCollaboratorJoined(memorialOwner, user, memorial);
                System.out.println("📧 Notificación enviada al dueño: " + memorialOwner.getEmail());
                
                // Notificar al nuevo colaborador
                notificationService.notifyJoinedAsCollaborator(user, memorial);
                System.out.println("📧 Notificación enviada al colaborador: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("⚠️ Error enviando notificaciones: " + e.getMessage());
                // No fallar la operación si las notificaciones fallan
            }
            
            System.out.println("✅ Colaborador agregado exitosamente");
            
            return buildCollaboratorResponse(collaborator);
            
        } catch (Exception e) {
            System.err.println("❌ Error aceptando código: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // ========== GENERAR CÓDIGO ==========
    
    @Override
    public InviteCodeResponse generateInviteCode(InviteCodeRequest request, User creator) {
        try {
            System.out.println("🔑 Generando código para memorial: " + request.getMemorialId());
            
            Memorial memorial = memorialRepository.findById(request.getMemorialId())
                    .orElseThrow(() -> new RuntimeException("Memorial no encontrado"));
            
            if (!memorial.getUser().getIdUser().equals(creator.getIdUser())) {
                throw new RuntimeException("Solo el dueño puede generar códigos");
            }
            
            String code = generateUniqueCode();
            
            InviteCode inviteCode = new InviteCode();
            inviteCode.setCode(code);
            inviteCode.setMemorial(memorial);
            inviteCode.setCreatedBy(creator);
            inviteCode.setCanEdit(request.getCanEdit() != null ? request.getCanEdit() : false);
            inviteCode.setCanComment(request.getCanComment() != null ? request.getCanComment() : true);
            inviteCode.setCreatedDate(LocalDateTime.now());
            inviteCode.setExpiresAt(LocalDateTime.now().plusHours(24));
            inviteCode.setStatus(InviteCodeStatusEnum.ACTIVE);
            inviteCode.setMaxUses(request.getMaxUses() != null ? request.getMaxUses() : 1);
            inviteCode.setUsedCount(0);
            
            inviteCode = inviteCodeRepository.save(inviteCode);
            
            System.out.println("✅ Código generado: " + code);
            
            return buildInviteCodeResponse(inviteCode);
            
        } catch (Exception e) {
            System.err.println("❌ Error generando código: " + e.getMessage());
            throw new RuntimeException("Error al generar código: " + e.getMessage(), e);
        }
    }

    // ========== VALIDAR CÓDIGO ==========
    
    @Override
    public InviteCodeValidationResponse validateInviteCode(String code) {
        try {
            System.out.println("🔍 Validando código: " + code);
            
            InviteCode inviteCode = inviteCodeRepository.findByCode(code.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Código inválido"));
            
            if (inviteCode.getStatus() != InviteCodeStatusEnum.ACTIVE) {
                throw new RuntimeException("Código no disponible");
            }
            
            if (LocalDateTime.now().isAfter(inviteCode.getExpiresAt())) {
                inviteCode.setStatus(InviteCodeStatusEnum.EXPIRED);
                inviteCodeRepository.save(inviteCode);
                throw new RuntimeException("Código expirado");
            }
            
            if (inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
                inviteCode.setStatus(InviteCodeStatusEnum.USED);
                inviteCodeRepository.save(inviteCode);
                throw new RuntimeException("Código agotado");
            }
            
            Memorial memorial = inviteCode.getMemorial();
            User creator = inviteCode.getCreatedBy();
            
            InviteCodeValidationResponse response = new InviteCodeValidationResponse();
            response.setMemorialId(memorial.getIdMemorial().toString());
            response.setMemorialName(memorial.getName());
            response.setMemorialDescription(memorial.getDescription());
            response.setInviterName(creator.getFirstName() + " " + creator.getFirstLastName());
            response.setCanEdit(inviteCode.getCanEdit());
            response.setCanComment(inviteCode.getCanComment());
            
            System.out.println("✅ Código válido");
            
            return response;
            
        } catch (Exception e) {
            System.err.println("❌ Error validando: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // ========== LISTAR COLABORADORES ==========
    
    @Override
public List<CollaboratorResponse> getCollaborators(UUID memorialId, User user) {
    try {
        System.out.println("📋 Listando colaboradores para memorial: " + memorialId);
        
        Memorial memorial = memorialRepository.findById(memorialId)
                .orElseThrow(() -> new RuntimeException("Memorial no encontrado"));
        
        boolean isOwner = memorial.getUser().getIdUser().equals(user.getIdUser());
        
        // ✅ CRÍTICO: Solo el dueño puede ver la lista de colaboradores
        if (!isOwner) {
            throw new RuntimeException("Solo el dueño puede gestionar colaboradores");
        }
        
        List<Collaborator> collaborators = collaboratorRepository
                .findByMemorialAndIsActiveTrue(memorial);
        
        System.out.println("✅ Colaboradores encontrados: " + collaborators.size());
        
        return collaborators.stream()
                .map(this::buildCollaboratorResponse)
                .collect(Collectors.toList());
                
    } catch (Exception e) {
        System.err.println("❌ Error listando colaboradores: " + e.getMessage());
        throw new RuntimeException(e.getMessage(), e);
    }
}

    // ========== ACTUALIZAR PERMISOS ==========
    
   @Override
public CollaboratorResponse updateCollaborator(Long collaboratorId, Boolean canEdit, 
                                              Boolean canComment, User user) {
    try {
        System.out.println("🔄 Actualizando colaborador " + collaboratorId);
        
        Collaborator collaborator = collaboratorRepository.findById(collaboratorId)
                .orElseThrow(() -> new RuntimeException("Colaborador no encontrado"));
        
        // ✅ CRÍTICO: Solo el dueño puede actualizar permisos
        if (!collaborator.getMemorial().getUser().getIdUser().equals(user.getIdUser())) {
            throw new RuntimeException("Solo el dueño puede actualizar permisos");
        }
        
        if (canEdit != null) {
            collaborator.setCanEdit(canEdit);
        }
        if (canComment != null) {
            collaborator.setCanComment(canComment);
        }
        
        collaborator = collaboratorRepository.save(collaborator);
        
        System.out.println("✅ Permisos actualizados para colaborador: " + collaborator.getIdCollaborator());
        
        return buildCollaboratorResponse(collaborator);
        
    } catch (Exception e) {
        System.err.println("❌ Error actualizando: " + e.getMessage());
        throw new RuntimeException(e.getMessage(), e);
    }
}

    // ========== ELIMINAR COLABORADOR ==========
    
    @Override
public void removeCollaborator(Long collaboratorId, User user) {
    try {
        System.out.println("🗑️ Eliminando colaborador " + collaboratorId);
        
        Collaborator collaborator = collaboratorRepository.findById(collaboratorId)
                .orElseThrow(() -> new RuntimeException("Colaborador no encontrado"));
        
        // ✅ CRÍTICO: Solo el dueño puede eliminar colaboradores
        if (!collaborator.getMemorial().getUser().getIdUser().equals(user.getIdUser())) {
            throw new RuntimeException("Solo el dueño puede eliminar colaboradores");
        }
        
        collaborator.setIsActive(false);
        collaboratorRepository.save(collaborator);
        
        System.out.println("✅ Colaborador eliminado: " + collaborator.getIdCollaborator());
        
    } catch (Exception e) {
        System.err.println("❌ Error eliminando: " + e.getMessage());
        throw new RuntimeException(e.getMessage(), e);
    }
}

    // ========== LISTAR CÓDIGOS ==========
    
    @Override
    public List<InviteCodeResponse> getInviteCodes(UUID memorialId, User user) {
        try {
            Memorial memorial = memorialRepository.findById(memorialId)
                    .orElseThrow(() -> new RuntimeException("Memorial no encontrado"));
            
            if (!memorial.getUser().getIdUser().equals(user.getIdUser())) {
                throw new RuntimeException("Solo el dueño puede ver códigos");
            }
            
            List<InviteCode> codes = inviteCodeRepository.findByMemorial(memorial);
            
            return codes.stream()
                    .map(this::buildInviteCodeResponse)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("❌ Error listando códigos: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // ========== REVOCAR CÓDIGO ==========
    
    @Override
    public void revokeInviteCode(String code, User user) {
        try {
            InviteCode inviteCode = inviteCodeRepository.findByCode(code.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Código no encontrado"));
            
            if (!inviteCode.getMemorial().getUser().getIdUser().equals(user.getIdUser())) {
                throw new RuntimeException("Solo el dueño puede revocar");
            }
            
            inviteCode.setStatus(InviteCodeStatusEnum.REVOKED);
            inviteCodeRepository.save(inviteCode);
            
            System.out.println("✅ Código revocado");
            
        } catch (Exception e) {
            System.err.println("❌ Error revocando: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // ========== HELPERS ==========
    
    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        
        do {
            code = generateRandomCode();
            attempts++;
            
            if (attempts >= 100) {
                throw new RuntimeException("No se pudo generar código único");
            }
        } while (inviteCodeRepository.existsByCode(code));
        
        return code;
    }
    
    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
    
    private CollaboratorResponse buildCollaboratorResponse(Collaborator collaborator) {
        CollaboratorResponse response = new CollaboratorResponse();
        response.setIdCollaborator(collaborator.getIdCollaborator());
        response.setEmail(collaborator.getUser().getEmail());
        response.setUserName(collaborator.getUser().getFirstName() + " " + 
                           collaborator.getUser().getFirstLastName());
        response.setCanEdit(collaborator.getCanEdit());
        response.setCanComment(collaborator.getCanComment());
        response.setInvitedDate(collaborator.getJoinedDate());
        response.setAcceptedDate(collaborator.getJoinedDate());
        response.setIsActive(collaborator.getIsActive());
        response.setStatus("active");
        
        return response;
    }
    
    private InviteCodeResponse buildInviteCodeResponse(InviteCode inviteCode) {
        InviteCodeResponse response = new InviteCodeResponse();
        response.setCode(inviteCode.getCode());
        response.setCanEdit(inviteCode.getCanEdit());
        response.setCanComment(inviteCode.getCanComment());
        response.setCreatedDate(inviteCode.getCreatedDate());
        response.setExpiresAt(inviteCode.getExpiresAt());
        response.setStatus(inviteCode.getStatus().name().toLowerCase());
        response.setMaxUses(inviteCode.getMaxUses());
        response.setUsedCount(inviteCode.getUsedCount());
        
        return response;
    }

    @Override
public List<MemorialResponse> getMyCollaborations(User user) {
    try {
        System.out.println("📋 getMyCollaborations llamado");
        System.out.println("   - User ID: " + user.getIdUser());
        System.out.println("   - User Email: " + user.getEmail());
        
        // ✅ Verificar que el usuario tenga ID
        if (user.getIdUser() == null) {
            System.err.println("❌ ERROR: User.idUser es NULL");
            return Collections.emptyList();
        }
        
        List<Memorial> memorials = memorialRepository.findMemorialsByCollaborator(user);
        
        System.out.println("✅ Query ejecutado correctamente");
        System.out.println("   - Memoriales encontrados: " + memorials.size());
        
        // Convertir a MemorialResponse
        List<MemorialResponse> responses = memorials.stream()
                .map(memorial -> {
                    try {
                        return buildMemorialResponse(memorial, user);
                    } catch (Exception e) {
                        System.err.println("❌ Error construyendo response para memorial: " + memorial.getIdMemorial());
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(response -> response != null) // Filtrar nulls
                .collect(Collectors.toList());
        
        System.out.println("✅ Responses construidos: " + responses.size());
        
        return responses;
        
    } catch (Exception e) {
        System.err.println("❌ Error en getMyCollaborations: " + e.getMessage());
        System.err.println("   - Clase de error: " + e.getClass().getName());
        e.printStackTrace();
        throw new RuntimeException("Error al obtener colaboraciones: " + e.getMessage(), e);
    }
}

/**
 * Helper para construir MemorialResponse desde Memorial
 */
private MemorialResponse buildMemorialResponse(Memorial memorial, User currentUser) {
    MemorialResponse response = new MemorialResponse();
    response.setIdMemorial(memorial.getIdMemorial());
    response.setName(memorial.getName());
    response.setNickname(memorial.getNickname());
    response.setBirthDate(memorial.getBirthDate());
    response.setGender(memorial.getGender());
    response.setDescription(memorial.getDescription());
    response.setRelationType(memorial.getRelationType());
    response.setCollaborative(memorial.isCollaborative());
    response.setJournal(memorial.isJournal());
    response.setCreatedDate(memorial.getCreatedDate());
    
    // ✅ CRÍTICO: Calcular isOwner
    boolean isOwner = memorial.getUser().getIdUser().equals(currentUser.getIdUser());
    response.setIsOwner(isOwner);
    
    System.out.println("🔍 Colaboración - Memorial: " + memorial.getName() + 
                     " | isOwner: " + isOwner);
    
    if (memorial.getProfilePhoto() != null) {
        File file = memorial.getProfilePhoto();
        response.setProfilePhoto(buildFileResponse(file));
    }
    
    return response;
}
/**
 * Invita directamente a un usuario por email.
 * Verifica si el email existe, crea el código y envía el email.
 */
@Override
public void inviteByEmail(
        UUID memorialId,
        String inviteeEmail,
        Boolean canEdit,
        Boolean canComment,
        User inviter) {
    
    try {
        System.out.println("📧 Invitando por email: " + inviteeEmail);
        
        // Verificar que el memorial existe y el usuario es dueño
        Memorial memorial = memorialRepository.findById(memorialId)
                .orElseThrow(() -> new RuntimeException("Memorial no encontrado"));
        
        if (!memorial.getUser().getIdUser().equals(inviter.getIdUser())) {
            throw new RuntimeException("Solo el dueño puede invitar");
        }
        
        // Verificar que el email existe en la plataforma
        User invitee = userRepository.findByEmail(inviteeEmail)
                .orElseThrow(() -> new RuntimeException(
                    "El usuario con email " + inviteeEmail + " no está registrado en Remory"
                ));
        
        // Verificar que no se invita a sí mismo
        if (invitee.getIdUser().equals(inviter.getIdUser())) {
            throw new RuntimeException("No puedes invitarte a ti mismo");
        }
        
        // Verificar que no sea ya colaborador
        if (collaboratorRepository.existsByUserAndMemorialAndIsActiveTrue(invitee, memorial)) {
            throw new RuntimeException("Este usuario ya es colaborador");
        }
        
        // Generar código de invitación
        String code = generateUniqueCode();
        
        InviteCode inviteCode = new InviteCode();
        inviteCode.setCode(code);
        inviteCode.setMemorial(memorial);
        inviteCode.setCreatedBy(inviter);
        inviteCode.setCanEdit(canEdit != null ? canEdit : false);
        inviteCode.setCanComment(canComment != null ? canComment : true);
        inviteCode.setCreatedDate(LocalDateTime.now());
        inviteCode.setExpiresAt(LocalDateTime.now().plusHours(24));
        inviteCode.setStatus(InviteCodeStatusEnum.ACTIVE);
        inviteCode.setMaxUses(1); // Solo para este usuario
        inviteCode.setUsedCount(0);
        
        inviteCode = inviteCodeRepository.save(inviteCode);
        
        // Enviar email de invitación
        String inviterName = inviter.getFirstName() + " " + inviter.getFirstLastName();
        
        emailService.sendMemorialInvitation(
            inviteeEmail,
            inviterName,
            memorial.getName(),
            code,
            inviteCode.getCanEdit(),
            inviteCode.getCanComment()
        );
        
        System.out.println("✅ Invitación enviada por email a: " + inviteeEmail);
        
    } catch (Exception e) {
        System.err.println("❌ Error invitando por email: " + e.getMessage());
        throw new RuntimeException(e.getMessage(), e);
    }
}

/**
 * Helper para construir FileResponse desde File
 */
private FileResponse buildFileResponse(File file) {
    FileResponse response = new FileResponse();
    response.setIdFile(file.getIdFile());
    response.setFileName(file.getFileName());
    response.setOriginalFileName(file.getOriginalFileName());
    response.setFileType(file.getFileType());
    response.setMimeType(file.getMimeType());
    response.setFileUrl(file.getFileUrl());
    response.setFileSize(file.getFileSize() != null ? file.getFileSize() / (1024 * 1024) : 0.0);
    response.setUploadedDate(file.getUploadedDate());
    return response;
}
}