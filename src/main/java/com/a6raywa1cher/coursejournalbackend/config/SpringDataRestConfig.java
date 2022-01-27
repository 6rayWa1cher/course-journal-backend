package com.a6raywa1cher.coursejournalbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Type;

@Configuration
public class SpringDataRestConfig {
    private final EntityManager entityManager;

    @Autowired
    public SpringDataRestConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Bean
    public RepositoryRestConfigurer repositoryRestConfigurer() {
        return RepositoryRestConfigurer.withConfig(config ->
                config.exposeIdsFor(
                        entityManager.getMetamodel().getEntities().stream()
                                .map(Type::getJavaType)
                                .toArray(Class[]::new)
                )
        );
    }
}