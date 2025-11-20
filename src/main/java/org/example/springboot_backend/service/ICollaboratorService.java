package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.*;
import org.example.springboot_backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface ICollaboratorService {
    
    List<MemorialResponse> getMyCollaborations(User user);
    // Gestión de códigos de invitación
    InviteCodeResponse generateInviteCode(InviteCodeRequest request, User creator);
    InviteCodeValidationResponse validateInviteCode(String code);
    CollaboratorResponse acceptInviteCode(String code, User user);
    List<InviteCodeResponse> getInviteCodes(UUID memorialId, User user);
    void revokeInviteCode(String code, User user);
    
    // Gestión de colaboradores
    List<CollaboratorResponse> getCollaborators(UUID memorialId, User user);
    CollaboratorResponse updateCollaborator(Long collaboratorId, Boolean canEdit, Boolean canComment, User user);
    void removeCollaborator(Long collaboratorId, User user);
    void inviteByEmail(UUID memorialId, String inviteeEmail, Boolean canEdit, Boolean canComment, User inviter);
}