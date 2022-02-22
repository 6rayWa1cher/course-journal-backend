package com.a6raywa1cher.coursejournalbackend.security;

import com.a6raywa1cher.jsonrestsecurity.web.JsonRestWebSecurityConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig extends JsonRestWebSecurityConfigurer {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        setUseAnyMatcher(false);

        super.configure(http);

        http.authorizeRequests()
                .anyRequest().access("hasAnyRole('TEACHER', 'ADMIN') && hasAuthority('ENABLED')");
    }
}
