package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    String displayUserForLoan(Loan loan) {
        return switch (loan) {
            case UnsecuredLoan(var interest) -> "oooh! that " + interest +
                    "% interest looks like it's going to hurt!";
            case SecuredLoan sl -> "hey! good job! well done.";
        };
    }

}

sealed interface Loan permits SecuredLoan, UnsecuredLoan {
}

final class SecuredLoan implements Loan {
}

record UnsecuredLoan(float interest) implements Loan {
}