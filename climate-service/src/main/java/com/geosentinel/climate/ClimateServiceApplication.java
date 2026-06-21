package com.geosentinel.climate;
import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
@SpringBootApplication @EnableScheduling
public class ClimateServiceApplication {
    public static void main(String[] args){SpringApplication.run(ClimateServiceApplication.class,args);}
    @Bean public RestTemplate restTemplate(){return new RestTemplate();}
}
