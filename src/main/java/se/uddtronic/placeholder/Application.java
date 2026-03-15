package se.uddtronic.placeholder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import se.uddtronic.placeholder.config.PlaceholderConfigProperties;

@SpringBootApplication
@EnableConfigurationProperties(PlaceholderConfigProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
