package bootiful.clients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestClientsApplication {

    @Bean
    @ServiceConnection
    @RestartScope
    PostgreSQLContainer<?> postgresContainer() {
        System.out.println("hello");
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
    }

    public static void main(String[] args) {
        SpringApplication.from(ClientsApplication::main).with(TestClientsApplication.class).run(args);
    }

}
