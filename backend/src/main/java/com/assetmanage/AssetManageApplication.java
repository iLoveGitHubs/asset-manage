package com.assetmanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AssetManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetManageApplication.class, args);
    }

    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"healthy\"}";
    }
}
