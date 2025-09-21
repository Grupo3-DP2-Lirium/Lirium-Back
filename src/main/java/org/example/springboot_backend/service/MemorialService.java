package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.MemorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemorialService implements IMemorialService {

    @Autowired
    private MemorialRepository memorialRepository;

    @Override
    public MemorialResponse createMemorial(Memorial memorial, User author) {
        memorial.setUser(author);
        memorial.setCreatedDate(LocalDateTime.now());
        memorial.setUpdatedDate(LocalDateTime.now());
        memorial.setUsedSpace(0.0);

        Memorial saved = memorialRepository.save(memorial);
        return buildResponse(saved);
    }



    @Override
    public List<MemorialResponse> getMemorialsByUser(User user) {
        return memorialRepository.findByUser(user)
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MemorialResponse getMemorialById(UUID id) {
        Memorial memorial = memorialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memorial not found"));
        return buildResponse(memorial);
    }

    @Override
    public void deleteMemorial(UUID id) {
        if (!memorialRepository.existsById(id)) {
            throw new RuntimeException("Memorial not found");
        }
        memorialRepository.deleteById(id);
    }

    private MemorialResponse buildResponse(Memorial memorial) {
        MemorialResponse response = new MemorialResponse();
        response.setIdMemorial(memorial.getIdMemorial());
        response.setName(memorial.getName());
        response.setNickname(memorial.getNickname());
        response.setBirthDate(memorial.getBirthDate());
        response.setGender(memorial.getGender());
        response.setDescription(memorial.getDescription());
        response.setRelationType(memorial.getRelationType());
        response.setProfilePhotoURL(memorial.getProfilePhotoURL());
        response.setCoverURL(memorial.getCoverURL());
        response.setCollaborative(memorial.isCollaborative());
        response.setJournal(memorial.isJournal());
        response.setUsedSpace(memorial.getUsedSpace());
        response.setCreatedDate(memorial.getCreatedDate());
        response.setUpdatedDate(memorial.getUpdatedDate());
        return response;
    }
}
