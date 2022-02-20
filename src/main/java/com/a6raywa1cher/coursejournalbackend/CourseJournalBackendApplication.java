package com.a6raywa1cher.coursejournalbackend;

import com.a6raywa1cher.jsonrestsecurity.rest.AuthController;
import org.springdoc.core.SpringDocUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CourseJournalBackendApplication {
    static {
        SpringDocUtils.getConfig().addRestControllers(AuthController.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(CourseJournalBackendApplication.class, args);
    }

}
