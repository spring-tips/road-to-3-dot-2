package bootiful.clients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Collection;

@SpringBootApplication
public class ClientsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientsApplication.class, args);
    }

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }

    @Bean
    CatFactClient catFactClient(RestClient restClient) {
        return HttpServiceProxyFactory
                .builder()
                .exchangeAdapter(RestClientAdapter.create(restClient))
                .build()
                .createClient(CatFactClient.class);
    }
}

@Controller
@ResponseBody
class CatFactController {

    private final CatFactClient facts;

    CatFactController(CatFactClient facts) {
        this.facts = facts;
    }

    @GetMapping("/catfact")
    CatFact fact() {
        return this.facts.fact();
    }
}

// https://catfact.ninja/fact


interface CatFactClient {

    @GetExchange("https://catfact.ninja/fact")
    CatFact fact();
}

record CatFact(String fact) {
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
        return this.repository.customers();
    }
}

@Repository
class CustomerRepository {

    private final JdbcClient jdbc;
    private final RowMapper<Customer> customerRowMapper =
            (rs, rowNum) -> new Customer(rs.getInt("id"), rs.getString("name"));

    CustomerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    Collection<Customer> customers() {
        return this.jdbc
                .sql("""
                            select * from customer
                        """)
                .query(this.customerRowMapper)
                .list();
    }
}

record Customer(Integer id, String name) {
}