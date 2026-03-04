package com.commerce4retail.product;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class ProductApiApplicationTest {

    @Test
    void contextLoads() {
        // Verifies Spring context loads without errors
    }
}
