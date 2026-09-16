package io.github.decadedx.smarthealthcare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("io.github.decadedx.smarthealthcare.mapper")
@SpringBootApplication
public class SmartHealthcareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHealthcareApplication.class, args);
    }

}
