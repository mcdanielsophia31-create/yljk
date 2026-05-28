package com.example.keshe1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.keshe1.mapper")
public class Keshe1Application {

    public static void main(String[] args) {
        SpringApplication.run(Keshe1Application.class, args);
    }

}
