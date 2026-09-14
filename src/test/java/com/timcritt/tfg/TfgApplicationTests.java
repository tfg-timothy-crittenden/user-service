package com.timcritt.tfg;

import config.PostgresTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Import(PostgresTestContainerConfiguration.class)
class TfgApplicationTests {

    @Test
    void contextLoads() {
    }
}
