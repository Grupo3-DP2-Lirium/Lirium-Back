package org.example.springboot_backend.config;

import jakarta.annotation.PostConstruct;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.CurrencyType;
import org.example.springboot_backend.entity.ExtraStoragePlan;
import org.example.springboot_backend.entity.Permission;
import org.example.springboot_backend.repository.PlanRepository;
import org.example.springboot_backend.repository.ExtraStoragePlanRepository;
import org.example.springboot_backend.repository.PermissionRepository;
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

    @Value("${paypal.plan.extra_10gb.monthly}")
    private String extra10GbMonthly;

    @Value("${paypal.plan.extra_50gb.monthly}")
    private String extra50GbMonthly;

    @Value("${paypal.plan.extra_100gb.monthly}")
    private String extra100GbMonthly;

    private final PlanRepository planRepository;
    private final PermissionRepository permissionRepository;
    private final ExtraStoragePlanRepository extraStoragePlanRepository;

    public DataInitializer(
            PlanRepository planRepository,
            PermissionRepository permissionRepository,
            ExtraStoragePlanRepository extraStoragePlanRepository
    ) {
        this.planRepository = planRepository;
        this.permissionRepository = permissionRepository;
        this.extraStoragePlanRepository = extraStoragePlanRepository;
    }

    @PostConstruct
    public void initData() {
        initPermissions();
        initPlans();
        initPlanPermissions();
        initExtraStoragePlans();
    }

    // ------------------- PERMISOS -------------------
    private void initPermissions() {
        if (permissionRepository.count() == 0) {
            Permission p1 = new Permission("VIEW_SHARED_MEMORIALS", "Puede ver memoriales compartidos");
            Permission p2 = new Permission("COLLABORATE_MEMORIALS", "Puede colaborar en memoriales");
            Permission p3 = new Permission("ACCESS_MY_PERSONAL_SPACE", "Acceso a espacio personal");
            Permission p4 = new Permission("CREATE_MEMORIALS", "Crea memoriales ilimitados");
            Permission p5 = new Permission("IA_FEATURES", "Acceso a funciones de IA");

            permissionRepository.saveAll(List.of(p1, p2, p3, p4, p5));
            System.out.println("Permisos creados correctamente.");
        } else {
            System.out.println("Los permisos ya existen, no se crearán duplicados.");
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
}
