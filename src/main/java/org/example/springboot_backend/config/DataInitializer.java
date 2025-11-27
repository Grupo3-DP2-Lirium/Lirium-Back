package org.example.springboot_backend.config;

import jakarta.annotation.PostConstruct;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.Role;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.Memory;
import org.example.springboot_backend.entity.CurrencyType;
import org.example.springboot_backend.entity.ExtraStoragePlan;
import org.example.springboot_backend.entity.Permission;
import org.example.springboot_backend.enums.UserStatus;
import org.example.springboot_backend.enums.MemoryOriginType;
import org.example.springboot_backend.repository.PlanRepository;
import org.example.springboot_backend.repository.ExtraStoragePlanRepository;
import org.example.springboot_backend.repository.PermissionRepository;
import org.example.springboot_backend.repository.RoleRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.MemoryRepository;
import org.example.springboot_backend.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${paypal.plan.create_share.monthly}")
    private String createShareMonthly;

    @Value("${paypal.plan.create_share.yearly}")
    private String createShareYearly;

    @Value("${paypal.plan.eternal_legacy.monthly}")
    private String eternoMonthly;

    @Value("${paypal.plan.eternal_legacy.yearly}")
    private String eternoYearly;

    @Value("${paypal.plan.extra_10gb.monthly}")
    private String extra10GbMonthly;

    @Value("${paypal.plan.extra_50gb.monthly}")
    private String extra50GbMonthly;

    @Value("${paypal.plan.extra_100gb.monthly}")
    private String extra100GbMonthly;

    private final PlanRepository planRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final ExtraStoragePlanRepository extraStoragePlanRepository;
    private final UserRepository userRepository;
    private final MemorialRepository memorialRepository;
    private final MemoryRepository memoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    private final Random random = new Random();

    public DataInitializer(
            PlanRepository planRepository,
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            ExtraStoragePlanRepository extraStoragePlanRepository,
            UserRepository userRepository,
            MemorialRepository memorialRepository,
            MemoryRepository memoryRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.planRepository = planRepository;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.extraStoragePlanRepository = extraStoragePlanRepository;
        this.userRepository = userRepository;
        this.memorialRepository = memorialRepository;
        this.memoryRepository = memoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initData() {
        initRole();
        initPermissions();
        initPlans();
        initPlanPermissions();
        assignAdminPermissions();
        initExtraStoragePlans();
        //initUsers();
        //initMemorials();
        //initMemories();
    }

    // ------------------- ROLES -------------------
    private void initRole() {
        if (roleRepository.count() == 0) {
            Role adminRole = roleService.createRoleIfNotExists("ADMIN");
            Role userRole = roleService.createRoleIfNotExists("USER");
            Role userPremiumRole = roleService.createRoleIfNotExists("PREMIUM");
            roleRepository.saveAll(List.of(adminRole, userRole, userPremiumRole));
            System.out.println("Roles creados correctamente.");
        } else {
            System.out.println("Los roles ya existen, no se crearán duplicados.");
        }
    }


    // ------------------- PERMISOS -------------------
    private void initPermissions() {
        if (permissionRepository.count() == 0) {
            Permission p1 = new Permission("VIEW_SHARED_MEMORIALS", "Puede ver memoriales compartidos");
            Permission p2 = new Permission("COLLABORATE_MEMORIALS", "Puede colaborar en memoriales");
            Permission p3 = new Permission("ACCESS_MY_PERSONAL_SPACE", "Acceso a espacio personal");
            Permission p4 = new Permission("CREATE_MEMORIALS", "Crea memoriales ilimitados");
            Permission p5 = new Permission("IA_FEATURES", "Acceso a funciones de IA");
            Permission p6 = new Permission("ADMIN_ACCESS", "Acceso al panel de administración");

            permissionRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6));
            System.out.println("Permisos creados correctamente.");
        } else {
            // Verificar si existe ADMIN_ACCESS, si no existe agregarlo
            if (permissionRepository.findByName("ADMIN_ACCESS").isEmpty()) {
                Permission adminAccess = new Permission("ADMIN_ACCESS", "Acceso al panel de administración");
                permissionRepository.save(adminAccess);
                System.out.println("Permiso ADMIN_ACCESS agregado.");
            }
            System.out.println("Los permisos ya existen, verificación completada.");
        }
    }

    // ------------------- PLANES -------------------
    private void initPlans() {
        if (planRepository.count() == 0) {

            Plan free = new Plan();
            free.setName("DESCUBRE_LIRIUM");
            free.setDescription("Acceso a tu espacio personal, visualización de memoriales compartidos , 1 colaboración (10 archivos)");
            free.setPrice(0.0);
            free.setActive(true);
            free.setStorageLimitGb(15.0);
            free.setMaxFiles(10);
            free.setMaxCollaborations(1);
            free.setMaxDocumentariesPerMonth(0);
            free.setCurrency(CurrencyType.USD.name());
            free.setSupportLevel("BASIC");

            Plan crea = new Plan();
            crea.setName("CREA_COMPARTE");
            crea.setDescription("Crea memoriales y colabora, acceso a mi espacio personal, 200 GB de almacenamiento, funcionalidades IA (línea de tiempo, organización y cápsulas), 1 documental al mes.");
            crea.setPrice(8.99);
            crea.setCurrency(CurrencyType.USD.name());
            crea.setActive(true);
            crea.setStorageLimitGb(200.0);
            crea.setMaxFiles(null); // null = ilimitado
            crea.setMaxCollaborations(null);
            crea.setMaxDocumentariesPerMonth(1);
            crea.setSupportLevel("STANDARD");
            crea.setPaypalPlanId(createShareMonthly); // o yearly según quieras inicializar

            Plan legado = new Plan();
            legado.setName("LEGADO_ETERNO");
            legado.setDescription("Todo lo del plan mensual, descuento 17%, 2 documentales extras y soporte prioritario.");
            legado.setPrice(89.99);
            legado.setCurrency(CurrencyType.USD.name());
            legado.setActive(true);
            legado.setActive(true);
            legado.setStorageLimitGb(2000.0);
            legado.setMaxFiles(null);
            legado.setMaxCollaborations(null);
            legado.setMaxDocumentariesPerMonth(2);
            legado.setSupportLevel("PRIORITY");
            legado.setPaypalPlanId(eternoYearly);

            planRepository.saveAll(List.of(free, crea, legado));
            System.out.println("Planes creados correctamente.");
        } else {
            System.out.println("Los planes ya existen, no se crearán duplicados.");
        }
    }

    // ------------------- PLAN - PERMISSION -------------------
    private void initPlanPermissions() {
        // Obtener planes por nombre
        Plan free = planRepository.findByName("DESCUBRE_LIRIUM").orElseThrow();
        Plan creaYComparte = planRepository.findByName("CREA_COMPARTE").orElseThrow();
        Plan legadoEterno = planRepository.findByName("LEGADO_ETERNO").orElseThrow();

        // Obtener permisos por nombre
        Permission viewShared = permissionRepository.findByName("VIEW_SHARED_MEMORIALS").orElseThrow();
        Permission collaborate = permissionRepository.findByName("COLLABORATE_MEMORIALS").orElseThrow();
        Permission personalSpace = permissionRepository.findByName("ACCESS_MY_PERSONAL_SPACE").orElseThrow();
        Permission createMemorials = permissionRepository.findByName("CREATE_MEMORIALS").orElseThrow();
        Permission iaFeatures = permissionRepository.findByName("IA_FEATURES").orElseThrow();
    
        // Free Plan: visualiza, colabora 1 vez, espacio personal
        free.setPermissions(Set.of(
            viewShared,
            collaborate,
            personalSpace
        ));

        // Crea y Comparte: todo lo del Free + crear memoriales + IA
        creaYComparte.setPermissions(Set.of(
            viewShared,
            collaborate,
            personalSpace,
            createMemorials,
            iaFeatures
        ));

        // Legado Eterno: todo lo anterior + soporte prioritario
        legadoEterno.setPermissions(Set.of(
            viewShared,
            collaborate,
            personalSpace,
            createMemorials,
            iaFeatures
        ));

        planRepository.saveAll(List.of(free, creaYComparte, legadoEterno));
        System.out.println("Permisos por plan asignados correctamente.");
    }

    // ------------------- PLANES DE ESPACIO EXTRA -------------------
    private void initExtraStoragePlans() {
        if (extraStoragePlanRepository.count() == 0) {
            ExtraStoragePlan extra10 = new ExtraStoragePlan();
            extra10.setName("EXTRA_10GB");
            extra10.setDescription("Espacio adicional de 10 GB");
            extra10.setAdditionalStorageGb(10);
            extra10.setPrice(1.99);
            extra10.setCurrency("USD");
            extra10.setActive(true);
            extra10.setPaypalPlanId(extra10GbMonthly);

            ExtraStoragePlan extra50 = new ExtraStoragePlan();
            extra50.setName("EXTRA_50GB");
            extra50.setDescription("Espacio adicional de 50 GB");
            extra50.setAdditionalStorageGb(50);
            extra50.setPrice(4.99);
            extra50.setCurrency("USD");
            extra50.setActive(true);
            extra50.setPaypalPlanId(extra50GbMonthly);

            ExtraStoragePlan extra100 = new ExtraStoragePlan();
            extra100.setName("EXTRA_100GB");
            extra100.setDescription("Espacio adicional de 100 GB");
            extra100.setAdditionalStorageGb(100);
            extra100.setPrice(7.99);
            extra100.setCurrency("USD");
            extra100.setActive(true);
            extra100.setPaypalPlanId(extra100GbMonthly);

            extraStoragePlanRepository.saveAll(List.of(extra10, extra50, extra100));
            System.out.println("Planes de espacio extra creados correctamente.");
        } else {
            System.out.println("Los planes de espacio extra ya existen.");
        }
    }

    // ------------------- ADMIN PERMISSIONS -------------------
    private void assignAdminPermissions() {
        try {
            // Buscar el rol ADMIN
            var adminRoleOpt = roleRepository.findByName("ADMIN");
            if (adminRoleOpt.isEmpty()) {
                System.out.println("⚠️ Rol ADMIN no encontrado, saltando asignación de permisos de admin");
                return;
            }

            Role adminRole = adminRoleOpt.get();
            
            // Buscar el permiso ADMIN_ACCESS
            var adminAccessOpt = permissionRepository.findByName("ADMIN_ACCESS");
            if (adminAccessOpt.isEmpty()) {
                System.out.println("⚠️ Permiso ADMIN_ACCESS no encontrado");
                return;
            }

            Permission adminAccess = adminAccessOpt.get();
            
            // Agregar el permiso al rol si no lo tiene
            Set<Permission> currentPermissions = adminRole.getPermissions();
            if (currentPermissions == null) {
                currentPermissions = new HashSet<>();
            }
            
            boolean wasAdded = currentPermissions.add(adminAccess);
            adminRole.setPermissions(currentPermissions);
            roleRepository.save(adminRole);
            
            if (wasAdded) {
                System.out.println("✅ Permiso ADMIN_ACCESS asignado al rol ADMIN");
            } else {
                System.out.println("ℹ️ El rol ADMIN ya tenía el permiso ADMIN_ACCESS");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al asignar permisos de admin: " + e.getMessage());
        }
    }

    // ------------------- USERS -------------------
    private void initUsers() {
        if (userRepository.count() > 0) {
            log.info("ℹ️  Users already exist, skipping seed");

            userRepository.findAll().forEach(user -> 
                            System.out.println("   - " + user.getEmail())
                        );

            return;
        }

        String[] firstNames = {"María", "Juan", "Ana", "Carlos", "Laura", "Miguel", "Carmen", "José", 
                              "Isabel", "David", "Lucía", "Javier", "Sofía", "Diego", "Elena"};
        String[] lastNames = {"García", "Rodríguez", "González", "Fernández", "López", "Martínez", 
                             "Sánchez", "Pérez", "Gómez", "Martín", "Ruiz", "Hernández", "Díaz"};

        List<User> users = new ArrayList<>();
        
        // Usuario admin
        users.add(createUser("admin@lirium.com", "admin123", "Admin", "Sistema", "ADMIN", firstNames, lastNames));

        // Usuarios de prueba
        for (int i = 1; i <= 25; i++) {
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + i + "@test.com";
            
            String role = "USER";
            if (i % 8 == 0) role = "ADMIN";
            else if (i % 4 == 0) role = "PREMIUM";
            
            users.add(createUser(email, "password123", firstName, lastName, role, firstNames, lastNames));
        }
        
        // Usuarios adicionales
        users.add(createUser("maria.test@admin.com", "password123", "María", "Administradora", "ADMIN", firstNames, lastNames));
        users.add(createUser("juan.premium@test.com", "password123", "Juan", "Premium", "PREMIUM", firstNames, lastNames));
        users.add(createUser("ana.inactive@test.com", "password123", "Ana", "Inactiva", "USER", firstNames, lastNames));
        users.add(createUser("carlos.olduser@test.com", "password123", "Carlos", "Antiguo", "USER", firstNames, lastNames));
        
        if (users.size() > 20) {
            users.get(users.size() - 2).setStatus(UserStatus.SUSPENDED);
        }

        userRepository.saveAll(users);
        createReflectionSpaces(users);
        
        log.info("🎉 Seeded {} users successfully!", users.size());
    }

    private User createUser(String email, String password, String firstName, String lastName, String roleName, String[] firstNames, String[] lastNames) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setFirstLastName(lastName);
        user.setSecondLastName(random.nextBoolean() ? lastNames[random.nextInt(lastNames.length)] : "");
        user.setStatus(UserStatus.ACTIVE);
        
        double maxSpace = 8192.0 * 1024 * 1024;
        double usedSpace = random.nextDouble() * maxSpace;
        user.setUsedSpace(usedSpace);
        user.setTotalCapacity(10240.0 * 1024 * 1024);
        
        LocalDate createdDate = LocalDate.now().minusDays(random.nextInt(365));
        user.setCreatedDate(createdDate);
        user.setUpdatedDate(createdDate.plusDays(random.nextInt((int) createdDate.until(LocalDate.now()).getDays() + 1)));
        
        if (random.nextBoolean()) {
            LocalDateTime lastSession = createdDate.atStartOfDay().plusDays(random.nextInt((int) createdDate.until(LocalDate.now()).getDays() + 1))
                    .plusHours(random.nextInt(24))
                    .plusMinutes(random.nextInt(60));
            user.setLastSessionDate(lastSession);
        }
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return user;
    }

    private void createReflectionSpaces(List<User> users) {
        log.info("🌱 Creating reflection spaces for users...");
        
        List<Memorial> reflectionSpaces = new ArrayList<>();
        
        for (User user : users) {
            Memorial reflectionSpace = new Memorial();
            reflectionSpace.setUser(user);
            reflectionSpace.setName("Mis Reflexiones Personales");
            reflectionSpace.setNickname("Reflexiones de " + user.getFirstName());
            reflectionSpace.setDescription("Espacio personal para reflexiones, pensamientos y momentos íntimos. Solo tú puedes ver y agregar contenido aquí.");
            reflectionSpace.setRelationType("Personal");
            reflectionSpace.setCollaborative(false);
            reflectionSpace.setJournal(true);
            reflectionSpace.setUsedSpace(0.0);
            reflectionSpace.setCreatedDate(LocalDateTime.now().minusDays(random.nextInt(5)));
            reflectionSpace.setUpdatedDate(LocalDateTime.now());
            
            reflectionSpaces.add(reflectionSpace);
        }
        
        memorialRepository.saveAll(reflectionSpaces);
        log.info("✅ Created {} reflection spaces!", reflectionSpaces.size());
    }

    // ------------------- MEMORIALS -------------------
    private void initMemorials() {
        if (memorialRepository.count() > 0) {
            log.info("ℹ️  Memorials already exist, skipping seed");
            return;
        }

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.info("⚠️  No users found, cannot create memorials");
            return;
        }

        String[] firstNames = {"María", "Juan", "Ana", "Carlos", "Laura", "Miguel", "Carmen", "José"};
        String[] lastNames = {"García", "Rodríguez", "González", "Fernández", "López", "Martínez"};
        String[] genders = {"Masculino", "Femenino"};
        String[] relationTypes = {"Padre", "Madre", "Hermano", "Hermana", "Abuelo", "Abuela", "Tío", "Tía", "Primo", "Prima", "Amigo", "Amiga"};

        List<Memorial> memorials = new ArrayList<>();

        for (User user : users) {
            int memorialCount = random.nextInt(3) + 1;
            
            for (int i = 0; i < memorialCount; i++) {
                Memorial memorial = new Memorial();
                memorial.setUser(user);
                
                String baseName = firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
                memorial.setName(baseName);
                memorial.setNickname(generateNickname(baseName));
                
                LocalDate birthDate = LocalDate.now().minusYears(random.nextInt(70) + 20);
                memorial.setBirthDate(birthDate);
                
                memorial.setGender(genders[random.nextInt(genders.length)]);
                memorial.setDescription("Un ser querido que siempre estará en nuestros corazones. Su memoria perdurará por siempre.");
                memorial.setRelationType(relationTypes[random.nextInt(relationTypes.length)]);
                memorial.setCollaborative(random.nextBoolean());
                memorial.setCreatedDate(LocalDateTime.now().minusDays(random.nextInt(15)));

                memorials.add(memorial);
            }
        }

        memorialRepository.saveAll(memorials);
        log.info("🎉 Seeded {} memorials successfully!", memorials.size());
    }

    private String generateNickname(String fullName) {
        String[] nicknamePrefixes = {"", "El ", "La ", "Querido ", "Querida "};
        String[] nicknameSuffixes = {"", " el Grande", " la Pequeña", " el Sabio", " la Alegre"};
        
        String firstName = fullName.split(" ")[0];
        String prefix = nicknamePrefixes[random.nextInt(nicknamePrefixes.length)];
        String suffix = nicknameSuffixes[random.nextInt(nicknameSuffixes.length)];
        
        return prefix + firstName + suffix;
    }

    // ------------------- MEMORIES -------------------
    private void initMemories() {
        if (memoryRepository.count() > 0) {
            log.info("ℹ️  Memories already exist, skipping seed");
            return;
        }

        List<Memorial> memorials = memorialRepository.findAll();
        if (memorials.isEmpty()) {
            log.info("⚠️  No memorials found, cannot create memories");
            return;
        }

        String[] memoryTitles = {"Primera sonrisa", "Día de graduación", "Vacaciones familiares", "Cumpleaños especial",
                                "Aventura en la playa", "Navidad en casa", "Paseo por el parque", "Cena familiar"};
        String[] memoryDescriptions = {"Un momento especial que siempre recordaremos con cariño",
                                      "Una experiencia única que marcó nuestras vidas",
                                      "Recuerdos llenos de alegría y felicidad"};
        String[] locations = {"Casa familiar", "Parque Central", "Playa de Valencia", "Restaurante El Jardín"};

        List<Memory> memories = new ArrayList<>();

        for (Memorial memorial : memorials) {
            int memoryCount = random.nextInt(5) + 2;
            
            for (int i = 0; i < memoryCount; i++) {
                Memory memory = new Memory();
                memory.setMemorial(memorial);
                memory.setAuthor(memorial.getUser());
                memory.setType(MemoryOriginType.SPONTANEOUS);
                memory.setTitle(memoryTitles[random.nextInt(memoryTitles.length)]);
                memory.setDescription(memoryDescriptions[random.nextInt(memoryDescriptions.length)]);
                
                LocalDate photoDate = LocalDate.now().minusDays(random.nextInt(730));
                memory.setPhotoDate(photoDate);
                
                memory.setLocation(locations[random.nextInt(locations.length)]);
                memory.setVisible(true);
                memory.setTags(generateRandomTagsList());
                memory.setAssociatedQuestion("");
                memory.setTotalUsedSpace(0.0);
                memory.setCreatedDate(LocalDateTime.now().minusDays(random.nextInt(10)));

                memories.add(memory);
            }
        }

        memoryRepository.saveAll(memories);
        log.info("🎉 Seeded {} memories successfully!", memories.size());
    }

    private List<String> generateRandomTagsList() {
        String[] tagOptions = {"familia", "alegría", "amor", "recuerdos", "felicidad", 
                              "amistad", "celebración", "nostalgia", "momentos", "especial"};
        
        int tagCount = random.nextInt(3) + 1;
        Set<String> selectedTags = new HashSet<>();
        
        for (int i = 0; i < tagCount; i++) {
            selectedTags.add(tagOptions[random.nextInt(tagOptions.length)]);
        }
        
        return new ArrayList<>(selectedTags);
    }
}
