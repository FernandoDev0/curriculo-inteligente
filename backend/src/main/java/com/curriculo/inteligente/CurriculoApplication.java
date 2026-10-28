package com.curriculo.inteligente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CurriculoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurriculoApplication.class, args);
    }
}
