package org.example.springboot_backend.config;

import jakarta.annotation.PostConstruct;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.enums.PlanType;
import org.example.springboot_backend.repository.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer {

    private final PlanRepository planRepository;
    private final PermissionRepository permissionRepository;
    private final PlanPermissionRepository planPermissionRepository;

    public DataInitializer(
            PlanRepository planRepository,
            PermissionRepository permissionRepository,
            PlanPermissionRepository planPermissionRepository
    ) {
        this.planRepository = planRepository;
        this.permissionRepository = permissionRepository;
        this.planPermissionRepository = planPermissionRepository;
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
        System.out.println("Verificando planes de suscripción...");

        if (planRepository.count() == 0) {
            Plan free = new Plan();
            free.setPlanType(PlanType.FREE);
            free.setFrequency(BillingPeriod.MONTHLY);
            free.setDescription("Acceso a tu memoria personal, 1 colaboración, y creación de 1 memorial.");
            free.setPrice(0.0);
            free.setCurrency("USD");
            free.setActive(true);

            Plan creaMensual = new Plan();
            creaMensual.setPlanType(PlanType.CREA_Y_COMPARTE);
            creaMensual.setFrequency(BillingPeriod.MONTHLY);
            creaMensual.setDescription("Crea memoriales ilimitados, 200 GB, IA, 1 documental al mes.");
            creaMensual.setPrice(8.99);
            creaMensual.setCurrency("USD");
            creaMensual.setActive(true);

            Plan legadoAnual = new Plan();
            legadoAnual.setPlanType(PlanType.LEGADO_ETERNO);
            legadoAnual.setFrequency(BillingPeriod.YEARLY);
            legadoAnual.setDescription("Todo lo del plan mensual, descuento 17%, 2 documentales extras y soporte prioritario.");
            legadoAnual.setPrice(89.99);
            legadoAnual.setCurrency("USD");
            legadoAnual.setActive(true);

            planRepository.saveAll(List.of(free, creaMensual, legadoAnual));

            System.out.println("Planes creados correctamente.");
        } else {
            System.out.println("Los planes ya existen, no se crearán duplicados.");
        }
    }

    // ------------------- PLAN - PERMISSION -------------------
    private void initPlanPermissions() {
        if (planPermissionRepository.count() == 0) {
            Plan free = planRepository.findByPlanType(PlanType.FREE).orElseThrow();
            Plan creaYComparte = planRepository.findByPlanType(PlanType.CREA_Y_COMPARTE).orElseThrow();
            Plan legadoEterno = planRepository.findByPlanType(PlanType.LEGADO_ETERNO).orElseThrow();

            planPermissionRepository.saveAll(List.of(
                    // FREE
                    new PlanPermission(free, "CREATE_ONE_MEMORIAL"),
                    new PlanPermission(free, "COLLABORATE_ONE_MEMORIAL"),
                    new PlanPermission(free, "USE_MY_PERSONAL_SPACE"),

                    // CREA_Y_COMPARTE
                    new PlanPermission(creaYComparte, "CREATE_MEMORIALS"),
                    new PlanPermission(creaYComparte, "IA_FUNCTIONS"),
                    new PlanPermission(creaYComparte, "CREATE_ONE_DOCUMENTAL"),

                    // LEGADO_ETERNO
                    new PlanPermission(legadoEterno, "CREATE_MEMORIALS"),
                    new PlanPermission(legadoEterno, "IA_FUNCTIONS"),
                    new PlanPermission(legadoEterno, "CREATE_TWO_DOCUMENTALS"),
                    new PlanPermission(legadoEterno, "PRIORITY_SUPPORT")
            ));

            System.out.println("Permisos por plan asignados correctamente.");
        } else {
            System.out.println("Ya existen permisos asignados a los planes, no se crearán duplicados.");
        }
    }

}
