package bootiful.boot32;

import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

//https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2.0-M1-Release-Notes#logged-application-name
@SpringBootApplication
public class Boot32Application {

    public static void main(String[] args) {
        SpringApplication.run(Boot32Application.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner (){
        return a -> System.out.println(new File(".").getAbsolutePath());
    }
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
class  GreetingsController {


    @Scheduled (fixedDelay = 1000)
    void scheduled (){
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
                        if (0 == index) observed.add(Thread.currentThread().toString());

                        Thread.sleep(100);
                        if (0 == index) observed.add(Thread.currentThread().toString());

                        Thread.sleep(100);
                        if (0 == index) observed.add(Thread.currentThread().toString());

                        Thread.sleep(100);
                        if (0 == index) observed.add(Thread.currentThread().toString());

                    }  //
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            for (var t : threads) t.start();

            for (var t : threads) t.join();

            System.out.println(observed);

        };
    }


    @Bean
    TaskDecorator taskDecorator() {
        return runnable -> () -> {
            System.out.println("decorator: running before the thread "+
                    Thread.currentThread());
            runnable.run();
            System.out.println("decorator: running before the thread "+
                    Thread.currentThread());
        };
    }
}
