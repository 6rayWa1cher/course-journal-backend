package com.a6raywa1cher.coursejournalbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Type;

@Configuration
public class SpringDataRestConfig {
    private final EntityManager entityManager;
    private final Validator validator;

    @Autowired
    public SpringDataRestConfig(EntityManager entityManager, Validator validator) {
        this.entityManager = entityManager;
        this.validator = validator;
    }

    @Bean
    public RepositoryRestConfigurer repositoryRestConfigurer() {
        return new MyRepositoryRestConfigurer();
    }

    private class MyRepositoryRestConfigurer implements RepositoryRestConfigurer {
        @Override
        public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
            config.exposeIdsFor(
                    entityManager.getMetamodel().getEntities().stream()
                            .map(Type::getJavaType)
                            .toArray(Class[]::new)
            );
//            config.getExposureConfiguration()
//                    .forDomainType(User.class)
//                    .withItemExposure(((metadata, httpMethods) ->
//                            httpMethods.disable(HttpMethod.PUT, HttpMethod.POST, HttpMethod.PATCH))
//                    );
        }

        @Override
        public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
            v.addValidator("beforeSave", validator);
        }

    }
}