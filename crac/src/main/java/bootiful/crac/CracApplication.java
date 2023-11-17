package bootiful.crac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
public class CracApplication {

    public static void main(String[] args) {
        SpringApplication.run(CracApplication.class, args);
    }


}

@Component
class StatefulThing implements SmartLifecycle {

    private final AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public void start() {

        if (this.started.compareAndSet(false, true))
            System.out.println("START");

    }

    @Override
    public void stop() {
        if (this.started.compareAndSet(true, false))
            System.out.println("STOP");
    }

    @Override
    public boolean isRunning() {
        return this.started.get();
    }
}
