package com.a6raywa1cher.coursejournalbackend.security;

import com.a6raywa1cher.coursejournalbackend.dto.CourseTokenDto;
import com.a6raywa1cher.coursejournalbackend.service.CourseTokenService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

public class CourseTokenAuthenticationProvider implements AuthenticationProvider {
    private final CourseTokenService courseTokenService;

    public CourseTokenAuthenticationProvider(CourseTokenService courseTokenService) {
        this.courseTokenService = courseTokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        try {
            CourseTokenAuthentication auth = (CourseTokenAuthentication) authentication;
            String token = auth.getCredentials();
            Optional<CourseTokenDto> byToken = courseTokenService.findByToken(token);
            if (byToken.isEmpty()) {
                throw new BadCredentialsException("Token isn't valid");
            }
            CourseTokenDto courseTokenDto = byToken.get();
            Long course = courseTokenDto.getCourse();
            List<GrantedAuthority> grantedAuthorityList = List.of(
                    new SimpleGrantedAuthority(Permission.getPermissionForCourse(course, ActionType.READ))
            );
            return new CourseTokenAuthentication(token, auth.getPrincipal(), grantedAuthorityList);
        } catch (Exception e) {
            authentication.setAuthenticated(false);
            throw e;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CourseTokenAuthentication.class.isAssignableFrom(authentication);
    }
}
