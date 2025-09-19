package org.example.springboot_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SpringbootBackendApplicationTests {

    @Test
    void contextLoads() {
        // Test básico que verifica que el contexto se puede cargar con BD
    }

}
