package work.yj1211.live;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication()
public class LiveApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiveApplication.class, args);
    }
}
