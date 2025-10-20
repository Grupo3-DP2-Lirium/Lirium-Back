package org.example.springboot_backend.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/storage-info")
    public ResponseEntity<Map<String, Object>> getStorageInfo() {
        Map<String, Object> info = new HashMap<>();
        
        String storageDir = System.getProperty("user.dir") + "/storage/";
        File storageFolder = new File(storageDir);
        
        info.put("storageDirectory", storageDir);
        info.put("exists", storageFolder.exists());
        info.put("isDirectory", storageFolder.isDirectory());
        info.put("canRead", storageFolder.canRead());
        
        if (storageFolder.exists() && storageFolder.isDirectory()) {
            File[] files = storageFolder.listFiles();
            info.put("fileCount", files != null ? files.length : 0);
            
            if (files != null && files.length > 0) {
                String[] fileNames = new String[Math.min(files.length, 10)]; // Mostrar máximo 10 archivos
                for (int i = 0; i < fileNames.length; i++) {
                    fileNames[i] = files[i].getName();
                }
                info.put("sampleFiles", fileNames);
            }
        }
        
        return ResponseEntity.ok(info);
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Server is running");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}