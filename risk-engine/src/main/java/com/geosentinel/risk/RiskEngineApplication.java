package com.geosentinel.risk;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
@SpringBootApplication @EnableCaching
public class RiskEngineApplication {
    public static void main(String[] args) { SpringApplication.run(RiskEngineApplication.class, args); }
    @Bean public RestTemplate restTemplate() { return new RestTemplate(); }
}
