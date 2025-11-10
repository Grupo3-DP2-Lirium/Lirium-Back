package org.example.springboot_backend.config;

import jakarta.annotation.PostConstruct;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.Role;
import org.example.springboot_backend.entity.CurrencyType;
import org.example.springboot_backend.entity.Permission;
import org.example.springboot_backend.repository.PlanRepository;
import org.example.springboot_backend.repository.PermissionRepository;
import org.example.springboot_backend.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer {

    @Value("${paypal.plan.create_share.monthly}")
    private String createShareMonthly;

    @Value("${paypal.plan.create_share.yearly}")
    private String createShareYearly;

    @Value("${paypal.plan.eternal_legacy.monthly}")
    private String eternoMonthly;

    @Value("${paypal.plan.eternal_legacy.yearly}")
    private String eternoYearly;

    private final PlanRepository planRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public DataInitializer(
            PlanRepository planRepository,
            PermissionRepository permissionRepository,
            RoleRepository roleRepository
    ) {
        this.planRepository = planRepository;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void initData() {
        initPermissions();
        initPlans();
        initPlanPermissions();
        assignAdminPermissions();
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
            free.setName("DESCUBRE_REMORY");
            free.setDescription("Acceso a tu espacio personal, visualización de memoriales compartidos , 1 colaboración (10 archivos)");
            free.setPrice(0.0);
            free.setActive(true);
            free.setStorageLimitGb(15);
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
            crea.setStorageLimitGb(200);
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
            legado.setStorageLimitGb(2000);
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
        Plan free = planRepository.findByName("DESCUBRE_REMORY").orElseThrow();
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
        System.out.println("✅ Permisos por plan asignados correctamente.");
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
}
