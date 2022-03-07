package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.EntityFactory;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.CriteriaService;
import com.a6raywa1cher.coursejournalbackend.service.TaskService;
import com.a6raywa1cher.coursejournalbackend.service.UserService;
import com.github.javafaker.Faker;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class IntegrationTestConfiguration {
    @Bean
    public Faker faker() {
        return new Faker();
    }

    @Bean
    public EntityFactory ef(TaskService taskService, CourseService courseService, UserService userService, CriteriaService criteriaService, Faker faker) {
        return new EntityFactory(taskService, courseService, userService, criteriaService, faker);
    }
}
