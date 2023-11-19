package bootiful.observability;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootApplication
public class ObservabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservabilityApplication.class, args);
    }


    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }

    @Bean
    CatFacts catFactClient(RestClient rc) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(rc))
                .build()
                .createClient(CatFacts.class);
    }
}

@Controller
@ResponseBody
class CatFactController {

    private final CatFacts facts;

    CatFactController(CatFacts facts) {
        this.facts = facts;
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/facts")
    CatFacts.CatFact catFacts() {
        var fact = this.facts.fact();
        log.info(fact.toString());
        return fact;
    }
}

interface CatFacts {

    @Observed(name = "client")
    @GetExchange("https://catfact.ninja/fact")
    CatFact fact();

    record CatFact(String fact) {
    }
}