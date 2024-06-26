package bootiful.boot32;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.task.TaskDecorator;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import javax.sql.DataSource;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

// https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2.0-M1-Release-Notes#logged-application-name
@SpringBootApplication
@RegisterReflectionForBinding (CatFact.class)
public class Boot32Application {

    public static void main(String[] args) {
        SpringApplication.run(Boot32Application.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner() {
        return arrrrrgImAPirate -> System.out.println(new File(".").getAbsolutePath());
    }
}

@Configuration
class JdbcConfiguration {

    @Bean
    JdbcClient jdbcClient(DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean
    ApplicationRunner customerServiceRunner(CustomerService customerService) {
        return args -> customerService.customers().forEach(System.out::println);
    }

}

@Configuration
class RestClientConfiguration {

    @Bean
    DeclarativeClientFacts declarativeClientFacts(RestClient restClient) {
        return HttpServiceProxyFactory
                .builder()
                .exchangeAdapter(RestClientAdapter.create(restClient))
                .build()
                .createClient(DeclarativeClientFacts.class);
    }

    @Bean
    ApplicationRunner catsRunner(Cats[] cats) {
        return args -> {
            var logger = LoggerFactory.getLogger(getClass());
            logger.info("---------------------------------------------------");
            for (var c : cats) {
                c.facts().forEach(System.out::println);
            }
        };
    }

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        // see https://github.com/spring-projects/spring-framework/issues/31590
        return builder.requestFactory(new JdkClientHttpRequestFactory()).build();
    }
}

interface Cats {
    Collection<CatFact> facts();
}


interface DeclarativeClientFacts extends Cats {

    @Override
    @GetExchange("https://cat-fact.herokuapp.com/facts/")
    Collection<CatFact> facts();
}

@Service
class RestClientCats implements Cats {

    private final String url = " https://cat-fact.herokuapp.com/facts/".trim();

    private final RestClient rest;

    private final ParameterizedTypeReference<Collection<CatFact>> cats = new ParameterizedTypeReference<Collection<CatFact>>() {
    };

    RestClientCats(RestClient rest) {
        this.rest = rest;
    }

    @Observed (name = "rest-client-cats")
    @Override
    public Collection<CatFact> facts() {
        LoggerFactory.getLogger(getClass()).info("calling the RestClient cats endpoint");
        var responseEntity = this.rest.get().uri(this.url).retrieve().toEntity(this.cats);
        Assert.state(responseEntity.getStatusCode().is2xxSuccessful(), "the request should've succeeded");
        return responseEntity.getBody();
    }
}

//curl --location 'https://cat-fact.herokuapp.com/facts/'
record CatFact(String text, @JsonProperty("_id") String id) {
}


@Service
@Transactional
class CustomerService {

    private final JdbcClient jdbc;

    CustomerService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    Collection<Customer> customers() {
        return this.jdbc
                .sql(" select * from CUSTOMER  ")
                .query((rs, rowNum) -> new Customer(rs.getInt("id"), rs.getString("name")))
                .list();
    }

}

@Controller
@ResponseBody
class CustomerController {

    private final CustomerService customerService;

    CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/customers")
    Collection<Customer> customers() {
        return this.customerService.customers();
    }
}

record Customer(Integer id, String name) {

}

/*
mkdir certs
cd certs
openssl req -x509 -subj "/CN=demo-cert-1" -keyout demo.key -out demo.crt -sha256 -days 365 -nodes -newkey ed25519

# tangent: does your curl work correctly?
i sometimes get "LibreSSL SSL_connect: SSL_ERROR_SYSCALL in connection to"
did you install curl with homebrew? i had trouble with built in curl on macos. make sure to install it
AND
run this:
  ```echo 'export PATH="/opt/homebrew/opt/curl/bin:$PATH"' >> ~/.zshrc```



* */
@Controller
@ResponseBody
class GreetingsController {

    @Scheduled(fixedDelay = 1000)
    void scheduled() {
        System.out.println("scheduled on thread " + Thread.currentThread());
    }

    @GetMapping("/hello")
    Map<String, String> hello() {
        return Map.of("greeting", "hello, world");
    }
}

@Service
@Async
class AsyncCustomerService {

    CompletableFuture<Instant> sendEmail() throws Exception {
        System.out.println("before sending email on thread " + Thread.currentThread());
        Thread.sleep(100);
        var instant = Instant.now();
        System.out.println("after sending email on thread " + Thread.currentThread());
        return CompletableFuture.completedFuture(instant);
    }
}

@EnableAsync
@Configuration
class LoomConfiguration {

    @Bean
    ApplicationRunner serviceRunner(AsyncCustomerService service) {
        return args -> service.sendEmail().thenAccept(instant -> System.out.println("got a result " + instant));
    }

    @Bean
    ApplicationRunner multipleThreadsRunner() {
        return args -> {
            var observed = new ConcurrentSkipListSet<String>();
            var threads = new ArrayList<Thread>();
            for (var i = 0; i < 1000; i++) {
                var index = i;
                threads.add(Thread.ofVirtual().unstarted(() -> {
                    try {
                        Thread.sleep(100);
                        if (0 == index) {
                            observed.add(Thread.currentThread().toString());
                        }

                        Thread.sleep(100);
                        if (0 == index) {
                            observed.add(Thread.currentThread().toString());
                        }

                        Thread.sleep(100);
                        if (0 == index) {
                            observed.add(Thread.currentThread().toString());
                        }

                        Thread.sleep(100);
                        if (0 == index) {
                            observed.add(Thread.currentThread().toString());
                        }

                    } //
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            for (var t : threads) {
                t.start();
            }

            for (var t : threads) {
                t.join();
            }

            System.out.println(observed);

        };
    }

    @Bean
    TaskDecorator taskDecorator() {
        return runnable -> () -> {
            System.out.println("decorator: running before the thread "
                    + Thread.currentThread());
            runnable.run();
            System.out.println("decorator: running before the thread "
                    + Thread.currentThread());
        };
    }
}
