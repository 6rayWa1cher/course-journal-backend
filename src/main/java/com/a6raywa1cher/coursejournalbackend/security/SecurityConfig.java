package com.a6raywa1cher.coursejournalbackend.security;

import com.a6raywa1cher.coursejournalbackend.service.CourseTokenService;
import com.a6raywa1cher.jsonrestsecurity.web.JsonRestWebSecurityConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig extends JsonRestWebSecurityConfigurer {
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final CourseTokenService courseTokenService;

    @Autowired
    public SecurityConfig(AuthenticationEntryPoint authenticationEntryPoint, CourseTokenService courseTokenService) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.courseTokenService = courseTokenService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        super.configure(auth);
        auth.authenticationProvider(new CourseTokenAuthenticationProvider(courseTokenService));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        setUseAnyMatcher(false);

        super.configure(http);

        http.authorizeRequests()
                .anyRequest().access("hasAnyRole('TEACHER', 'ADMIN') && hasAuthority('ENABLED')");

        http.addFilterBefore(
                new CourseTokenAuthenticationFilter(
                        authenticationManagerBean(),
                        authenticationEntryPoint
                ),
                UsernamePasswordAuthenticationFilter.class
        );
    }
}
