package bootiful.restart;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RestartApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestartApplication.class, args);
	}

	@Bean
	ApplicationRunner  applicationRunner (){
		return a -> System.out.println("hello, Devtools!");
	}

}
