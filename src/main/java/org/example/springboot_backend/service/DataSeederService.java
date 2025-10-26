package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.enums.MemoryOriginType;
import org.example.springboot_backend.enums.UserStatus;
import org.example.springboot_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Profile("dev")
@Transactional
public class DataSeederService {

    @Autowired
    private RoleService roleService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MemorialRepository memorialRepository;
    
    @Autowired
    private MemoryRepository memoryRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    // Arrays para generar datos aleatorios
    private final String[] firstNames = {
        "María", "Juan", "Ana", "Carlos", "Laura", "Miguel", "Carmen", "José", 
        "Isabel", "David", "Lucía", "Javier", "Sofía", "Diego", "Elena"
    };

    private final String[] lastNames = {
        "García", "Rodríguez", "González", "Fernández", "López", "Martínez", 
        "Sánchez", "Pérez", "Gómez", "Martín", "Ruiz", "Hernández", "Díaz"
    };



    private final String[] memoryTitles = {
        "Primera sonrisa", "Día de graduación", "Vacaciones familiares", "Cumpleaños especial",
        "Aventura en la playa", "Navidad en casa", "Paseo por el parque", "Cena familiar",
        "Viaje inolvidable", "Momento de risas", "Tarde de domingo", "Celebración especial"
    };

    private final String[] memoryDescriptions = {
        "Un momento especial que siempre recordaremos con cariño",
        "Una experiencia única que marcó nuestras vidas",
        "Recuerdos llenos de alegría y felicidad",
        "Un día perfecto que permanecerá en nuestros corazones",
        "Momentos de pura felicidad compartidos en familia",
        "Una aventura que nos unió aún más",
        "Risas, amor y buenos momentos juntos",
        "Un recuerdo precioso que atesoraremos para siempre"
    };

    private final String[] locations = {
        "Casa familiar", "Parque Central", "Playa de Valencia", "Restaurante El Jardín",
        "Montañas de Asturias", "Plaza Mayor", "Jardín Botánico", "Casa de los abuelos",
        "Centro comercial", "Cine Palacio", "Biblioteca municipal", "Hospital Regional"
    };

    public void seedRoles() {
        System.out.println("🌱 Seeding roles...");
        
        if (roleService.countRoles() > 0) {
            System.out.println("ℹ️  Roles already exist, skipping seed");
            return;
        }

        List<String> roleNames = Arrays.asList("USER", "ADMIN", "PREMIUM");
        
        for (String roleName : roleNames) {
            Role role = roleService.createRoleIfNotExists(roleName);
            System.out.println("✅ Created role: " + role.getName());
        }
        
        System.out.println("🎉 Seeded " + roleNames.size() + " roles successfully!");
    }

    public void seedUsers() {
        System.out.println("🌱 Seeding users...");
        
        if (userRepository.count() > 0) {
            System.out.println("ℹ️  Users already exist, skipping seed");
            return;
        }

        // Asegurar que existen los roles
        seedRoles();

        List<User> users = new ArrayList<>();
        
        // Crear usuario administrador
        users.add(createUser(
            "admin@lirium.com", 
            "admin123",
            "Admin", 
            "Sistema",
            "ADMIN"
        ));

        // Crear usuarios de prueba
        for (int i = 1; i <= 5; i++) {
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + i + "@test.com";
            
            users.add(createUser(
                email,
                "password123",
                firstName,
                lastName,
                "USER"
            ));
        }

        userRepository.saveAll(users);
        
        // Crear espacios de reflexiones para cada usuario
        createReflectionSpacesForUsers(users);
        
        System.out.println("🎉 Seeded " + users.size() + " users successfully!");
    }

    public void seedMemorials() {
        System.out.println("🌱 Seeding memorials...");
        
        if (memorialRepository.count() > 0) {
            System.out.println("ℹ️  Memorials already exist, skipping seed");
            return;
        }

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            System.out.println("⚠️  No users found, seeding users first...");
            seedUsers();
            users = userRepository.findAll();
        }

        List<Memorial> memorials = new ArrayList<>();

        for (User user : users) {
            // Crear 1-3 memoriales por usuario
            int memorialCount = random.nextInt(3) + 1;
            
            for (int i = 0; i < memorialCount; i++) {
                Memorial memorial = createMemorial(user);
                memorials.add(memorial);
            }
        }

        memorialRepository.saveAll(memorials);
        System.out.println("🎉 Seeded " + memorials.size() + " memorials successfully!");
    }

    public void seedMemories() {
        System.out.println("🌱 Seeding memories...");
        
        if (memoryRepository.count() > 0) {
            System.out.println("ℹ️  Memories already exist, skipping seed");
            return;
        }

        List<Memorial> memorials = memorialRepository.findAll();
        if (memorials.isEmpty()) {
            System.out.println("⚠️  No memorials found, seeding memorials first...");
            seedMemorials();
            memorials = memorialRepository.findAll();
        }

        List<Memory> memories = new ArrayList<>();

        for (Memorial memorial : memorials) {
            // Crear 2-6 memorias por memorial
            int memoryCount = random.nextInt(5) + 2;
            
            for (int i = 0; i < memoryCount; i++) {
                Memory memory = createMemory(memorial);
                memories.add(memory);
            }
        }

        memoryRepository.saveAll(memories);
        System.out.println("🎉 Seeded " + memories.size() + " memories successfully!");
    }

    public void seedAll() {
        System.out.println("🌱 Starting complete seed process...");
        seedRoles();
        seedUsers();
        seedMemorials();
        seedMemories();
        System.out.println("🎉 Complete seed process finished successfully!");
    }

    public void clearAllData() {
        System.out.println("🧹 Clearing all seeded data...");
        memoryRepository.deleteAll();
        memorialRepository.deleteAll();
        userRepository.deleteAll();
        // No borramos roles ya que son fundamentales para el sistema
        System.out.println("✅ All data cleared successfully!");
    }

    public Map<String, Object> createCompleteUserWithData() {
        System.out.println("🌱 Creating complete user with random data...");
        
        // Asegurar que existen los roles
        seedRoles();

        // Crear usuario aleatorio
        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];
        String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                      System.currentTimeMillis() + "@random.com";
        
        User user = createUser(email, "password123", firstName, lastName, "USER");
        user = userRepository.save(user);

        // Crear 2-4 memoriales para este usuario
        List<Memorial> memorials = new ArrayList<>();
        int memorialCount = random.nextInt(3) + 2;
        
        for (int i = 0; i < memorialCount; i++) {
            Memorial memorial = createMemorial(user);
            memorial = memorialRepository.save(memorial);
            memorials.add(memorial);
        }

        // Crear memorias para cada memorial
        List<Memory> memories = new ArrayList<>();
        for (Memorial memorial : memorials) {
            int memoryCount = random.nextInt(4) + 3; // 3-6 memorias por memorial
            
            for (int i = 0; i < memoryCount; i++) {
                Memory memory = createMemory(memorial);
                memories.add(memory);
            }
        }
        memoryRepository.saveAll(memories);

        Map<String, Object> result = new HashMap<>();
        result.put("user", Map.of(
            "id", user.getIdUser().toString(),
            "email", user.getEmail(),
            "name", user.getFirstName() + " " + user.getFirstLastName()
        ));
        result.put("memorialsCount", memorials.size());
        result.put("memoriesCount", memories.size());
        result.put("memorials", memorials.stream().map(m -> Map.of(
            "id", m.getIdMemorial().toString(),
            "name", m.getName(),
            "memoriesCount", memories.stream()
                    .filter(mem -> mem.getMemorial().getIdMemorial().equals(m.getIdMemorial()))
                    .count()
        )).toList());

        System.out.println("🎉 Created complete user: " + user.getEmail() + 
                          " with " + memorials.size() + " memorials and " + 
                          memories.size() + " memories!");
        
        return result;
    }

    private User createUser(String email, String password, String firstName, String lastName, String roleName) {
        Role role = roleService.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setFirstLastName(lastName);
        user.setSecondLastName(""); // Opcional
        user.setStatus(UserStatus.ACTIVE);
        user.setUsedSpace(0.0);
        user.setTotalCapacity(10240.0 * 1024 * 1024); // 10 GB en bytes
        user.setCreatedDate(LocalDate.now().minusDays(random.nextInt(30))); // Fecha aleatoria últimos 30 días
        user.setUpdatedDate(LocalDate.now());
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return user;
    }

    private Memorial createMemorial(User user) {
        String[] genders = {"Masculino", "Femenino"};
        String[] relationTypes = {"Padre", "Madre", "Hermano", "Hermana", "Abuelo", "Abuela", 
                                 "Tío", "Tía", "Primo", "Prima", "Amigo", "Amiga"};

        Memorial memorial = new Memorial();
        memorial.setUser(user);
        
        String baseName = firstNames[random.nextInt(firstNames.length)] + " " + 
                         lastNames[random.nextInt(lastNames.length)];
        memorial.setName(baseName);
        memorial.setNickname(generateNickname(baseName));
        
        // Fecha de nacimiento aleatoria (entre 20 y 90 años atrás)
        LocalDate birthDate = LocalDate.now().minusYears(random.nextInt(70) + 20);
        memorial.setBirthDate(birthDate);
        
        memorial.setGender(genders[random.nextInt(genders.length)]);
        memorial.setDescription("Un ser querido que siempre estará en nuestros corazones. " +
                               "Su memoria perdurará por siempre.");
        memorial.setRelationType(relationTypes[random.nextInt(relationTypes.length)]);
        memorial.setCollaborative(random.nextBoolean());
        memorial.setCreatedDate(LocalDateTime.now().minusDays(random.nextInt(15)));

        return memorial;
    }

    private Memory createMemory(Memorial memorial) {
        Memory memory = new Memory();
        memory.setMemorial(memorial);
        memory.setAuthor(memorial.getUser());
        memory.setType(MemoryOriginType.SPONTANEOUS); // Memoria espontánea
        memory.setTitle(memoryTitles[random.nextInt(memoryTitles.length)]);
        memory.setDescription(memoryDescriptions[random.nextInt(memoryDescriptions.length)]);
        
        // Fecha de la foto/memoria aleatoria (últimos 2 años)
        LocalDate photoDate = LocalDate.now().minusDays(random.nextInt(730));
        memory.setPhotoDate(photoDate);
        
        memory.setLocation(locations[random.nextInt(locations.length)]);
        memory.setVisible(true);
        memory.setTags(generateRandomTagsList());
        memory.setAssociatedQuestion(""); // Vacío por defecto
        memory.setTotalUsedSpace(0.0); // Sin archivos por defecto
        memory.setCreatedDate(LocalDateTime.now().minusDays(random.nextInt(10)));

        return memory;
    }

    private String generateNickname(String fullName) {
        String[] nicknamePrefixes = {"", "El ", "La ", "Querido ", "Querida "};
        String[] nicknameSuffixes = {"", " el Grande", " la Pequeña", " el Sabio", " la Alegre"};
        
        String firstName = fullName.split(" ")[0];
        String prefix = nicknamePrefixes[random.nextInt(nicknamePrefixes.length)];
        String suffix = nicknameSuffixes[random.nextInt(nicknameSuffixes.length)];
        
        return prefix + firstName + suffix;
    }

    private List<String> generateRandomTagsList() {
        String[] tagOptions = {"familia", "alegría", "amor", "recuerdos", "felicidad", 
                              "amistad", "celebración", "nostalgia", "momentos", "especial"};
        
        int tagCount = random.nextInt(3) + 1; // 1-3 tags
        Set<String> selectedTags = new HashSet<>();
        
        for (int i = 0; i < tagCount; i++) {
            selectedTags.add(tagOptions[random.nextInt(tagOptions.length)]);
        }
        
        return new ArrayList<>(selectedTags);
    }

    private void createReflectionSpacesForUsers(List<User> users) {
        System.out.println("🌱 Creating reflection spaces for users...");
        
        List<Memorial> reflectionSpaces = new ArrayList<>();
        
        for (User user : users) {
            Memorial reflectionSpace = new Memorial();
            reflectionSpace.setUser(user);
            reflectionSpace.setName("Mis Reflexiones Personales");
            reflectionSpace.setNickname("Reflexiones de " + user.getFirstName());
            reflectionSpace.setDescription("Espacio personal para reflexiones, pensamientos y momentos íntimos. Solo tú puedes ver y agregar contenido aquí.");
            reflectionSpace.setRelationType("Personal");
            reflectionSpace.setCollaborative(false); // Solo el usuario puede agregar contenido
            reflectionSpace.setJournal(true); // Marcarlo como diario personal
            reflectionSpace.setUsedSpace(0.0);
            reflectionSpace.setCreatedDate(LocalDateTime.now().minusDays(random.nextInt(5))); // Fecha aleatoria últimos 5 días
            reflectionSpace.setUpdatedDate(LocalDateTime.now());
            
            reflectionSpaces.add(reflectionSpace);
        }
        
        memorialRepository.saveAll(reflectionSpaces);
        System.out.println("✅ Created " + reflectionSpaces.size() + " reflection spaces!");
    }
}