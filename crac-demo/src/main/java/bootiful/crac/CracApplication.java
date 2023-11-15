package bootiful.crac;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.HikariCheckpointRestoreLifecycle;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

@SpringBootApplication
public class CracApplication {

    public static void main(String[] args) {
        SpringApplication.run(CracApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(HikariCheckpointRestoreLifecycle hikariCheckpointRestoreLifecycle,
                                        HikariDataSource hdb, CustomerRepository repository) {
        return args -> {

            System.out.println("hikari checkpoint: " + hikariCheckpointRestoreLifecycle.isRunning());
            System.out.println("hdb: " + hdb.isRunning());

            repository.findAll().forEach(System.out::println);
        };
    }
}

@Controller
@ResponseBody
class CustomerController {

    private final CustomerRepository repository;

    CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/customers")
    Collection<Customer> customers() {
        return this.repository.findAll();
    }
}

record Customer(@Id Integer id, String name) {
}

interface CustomerRepository extends ListCrudRepository<Customer, Integer> {
}

@Component
class MyLifecycle implements Lifecycle {

    @Override
    public void start() {
        System.out.println("start");
    }

    @Override
    public void stop() {
        System.out.println("stop");
    }

    @Override
    public boolean isRunning() {
        return true;
    }
}