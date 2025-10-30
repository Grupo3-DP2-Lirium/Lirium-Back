package org.example.springboot_backend.config;

import jakarta.annotation.PostConstruct;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.CurrencyType;
import org.example.springboot_backend.entity.Permission;
import org.example.springboot_backend.repository.PlanRepository;
import org.example.springboot_backend.repository.PermissionRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer {

    private final PlanRepository planRepository;
    private final PermissionRepository permissionRepository;

    public DataInitializer(
            PlanRepository planRepository,
            PermissionRepository permissionRepository
    ) {
        this.planRepository = planRepository;
        this.permissionRepository = permissionRepository;
    }

    @PostConstruct
    public void initData() {
        initPermissions();
        initPlans();
        initPlanPermissions();
    }

    // ------------------- PERMISOS -------------------
    private void initPermissions() {
        if (permissionRepository.count() == 0) {
            Permission p1 = new Permission("CREATE_ONE_MEMORIAL", "Puede crear un memorial");
            Permission p2 = new Permission("COLLABORATE_ONE_MEMORIAL", "Puede colaborar en un memorial");
            Permission p3 = new Permission("USE_MY_PERSONAL_SPACE", "Acceso a espacio personal");
            Permission p4 = new Permission("CREATE_MEMORIALS", "Crea memoriales ilimitados");
            Permission p5 = new Permission("IA_FUNCTIONS", "Acceso a funciones de IA");
            Permission p6 = new Permission("CREATE_ONE_DOCUMENTAL", "Crea un documental al mes");
            Permission p7 = new Permission("CREATE_TWO_DOCUMENTALS", "Crea dos documentales extra");
            Permission p8 = new Permission("PRIORITY_SUPPORT", "Acceso a soporte prioritario");

            permissionRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7, p8));
            System.out.println("Permisos creados correctamente.");
        } else {
            System.out.println("Los permisos ya existen, no se crearán duplicados.");
        }
    }

    // ------------------- PLANES -------------------
    private void initPlans() {
        if (planRepository.count() == 0) {

            Plan free = new Plan();
            free.setName("FREE");
            free.setDescription("Acceso a tu memoria personal, 1 colaboración, y creación de 1 memorial.");
            free.setPrice(0.0);
            free.setCurrency(CurrencyType.USD.name());
            free.setActive(true);

            Plan creaMensual = new Plan();
            creaMensual.setName("CREA_COMPARTE");
            creaMensual.setDescription("Crea memoriales ilimitados, 200 GB, IA, 1 documental al mes.");
            creaMensual.setPrice(8.99);
            creaMensual.setCurrency(CurrencyType.USD.name());
            creaMensual.setActive(true);

            Plan legadoAnual = new Plan();
            legadoAnual.setName("LEGADO_ETERNO");
            legadoAnual.setDescription("Todo lo del plan mensual, descuento 17%, 2 documentales extras y soporte prioritario.");
            legadoAnual.setPrice(89.99);
            legadoAnual.setCurrency(CurrencyType.USD.name());
            legadoAnual.setActive(true);

            planRepository.saveAll(List.of(free, creaMensual, legadoAnual));
            System.out.println("Planes creados correctamente.");
        } else {
            System.out.println("Los planes ya existen, no se crearán duplicados.");
        }
    }

    // ------------------- PLAN - PERMISSION -------------------
    private void initPlanPermissions() {
        // Obtener planes por nombre
        Plan free = planRepository.findByName("FREE").orElseThrow();
        Plan creaYComparte = planRepository.findByName("CREA_COMPARTE").orElseThrow();
        Plan legadoEterno = planRepository.findByName("LEGADO_ETERNO").orElseThrow();

        // Obtener permisos por nombre
        Permission pCreateOne = permissionRepository.findByName("CREATE_ONE_MEMORIAL").orElseThrow();
        Permission pCollaborateOne = permissionRepository.findByName("COLLABORATE_ONE_MEMORIAL").orElseThrow();
        Permission pUseSpace = permissionRepository.findByName("USE_MY_PERSONAL_SPACE").orElseThrow();
        Permission pCreateMemorials = permissionRepository.findByName("CREATE_MEMORIALS").orElseThrow();
        Permission pIAFunctions = permissionRepository.findByName("IA_FUNCTIONS").orElseThrow();
        Permission pCreateOneDoc = permissionRepository.findByName("CREATE_ONE_DOCUMENTAL").orElseThrow();
        Permission pCreateTwoDocs = permissionRepository.findByName("CREATE_TWO_DOCUMENTALS").orElseThrow();
        Permission pPrioritySupport = permissionRepository.findByName("PRIORITY_SUPPORT").orElseThrow();

        // Asignar permisos a planes usando ManyToMany
        free.setPermissions(new HashSet<>(Set.of(pCreateOne, pCollaborateOne, pUseSpace)));
        creaYComparte.setPermissions(new HashSet<>(Set.of(pCreateMemorials, pIAFunctions, pCreateOneDoc)));
        legadoEterno.setPermissions(new HashSet<>(Set.of(pCreateMemorials, pIAFunctions, pCreateTwoDocs, pPrioritySupport)));

        planRepository.saveAll(List.of(free, creaYComparte, legadoEterno));
        System.out.println("Permisos por plan asignados correctamente.");
    }
}
