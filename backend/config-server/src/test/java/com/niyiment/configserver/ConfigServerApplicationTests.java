package com.niyiment.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.config.server.git.clone-on-start=false",
        "spring.cloud.config.server.git.uri=file:./"
})
@ActiveProfiles("test")
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
