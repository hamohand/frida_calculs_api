package com.med.frida_calculs_app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class FridaCalculsAppApplication {

    public static void main(String[] args) {

        SpringApplication.run(FridaCalculsAppApplication.class, args);
        log.info("Frida calcul - port:8081 -");
        System.out.println("Frida Calculs App - port:8080 -");
    }

}
