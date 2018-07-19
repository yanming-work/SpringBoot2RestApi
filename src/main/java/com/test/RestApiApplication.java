package com.test;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling 
@ComponentScan(basePackages = { "com.test"})
@MapperScan("com.test.web.dao")
public class RestApiApplication implements HealthIndicator{  

    public static void main(String[] args) {
        SpringApplication.run(RestApiApplication.class, args);
        
   
        
    }

    /** 
     * 在/health接口调用的时候，返回多一个属性："mySpringBootApplication":{"status":"UP","hello":"world"} 
     */  
    @Override  
    public Health health() {  
        return Health.up().withDetail("hello", "world").build();  
    }  
}

