package org.example.springboot_backend.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class DefaultBackgroundService {
    
    private final List<String> defaultBackgrounds = Arrays.asList(
        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&h=800&fit=crop", // Montañas al atardecer
        "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=1200&h=800&fit=crop", // Bosque verde
        "https://images.unsplash.com/photo-1519904981063-b0cf448d479e?w=1200&h=800&fit=crop", // Playa al atardecer
        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1200&h=800&fit=crop", // Lago con montañas
        "https://images.unsplash.com/photo-1433838552652-f9a46b332c40?w=1200&h=800&fit=crop", // Campo de flores
        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&h=800&fit=crop", // Cielo estrellado
        "https://images.unsplash.com/photo-1511593358241-7eea1f3c84e5?w=1200&h=800&fit=crop", // Jardín zen
        "https://images.unsplash.com/photo-1518837695005-2083093ee35b?w=1200&h=800&fit=crop", // Océano tranquilo
        "https://images.unsplash.com/photo-1501594907352-04cda38ebc29?w=1200&h=800&fit=crop", // Bosque otoñal
        "https://images.unsplash.com/photo-1502780402662-acc01917424e?w=1200&h=800&fit=crop"  // Valle sereno
    );
    
    private final Random random = new Random();
    
    /**
     * Obtiene una imagen de fondo aleatoria de paisajes serenos
     * @return URL de una imagen de fondo por defecto
     */
    public String getRandomBackground() {
        return defaultBackgrounds.get(random.nextInt(defaultBackgrounds.size()));
    }
    
    /**
     * Obtiene una imagen de fondo basada en el ID del memorial (consistente)
     * @param memorialId ID del memorial para generar un fondo consistente
     * @return URL de una imagen de fondo por defecto
     */
    public String getBackgroundForMemorial(java.util.UUID memorialId) {
        // Usar el hashCode del UUID para generar un índice consistente
        int index = Math.abs(memorialId.hashCode()) % defaultBackgrounds.size();
        return defaultBackgrounds.get(index);
    }
}