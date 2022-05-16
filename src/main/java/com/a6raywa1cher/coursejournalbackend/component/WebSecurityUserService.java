package com.a6raywa1cher.coursejournalbackend.component;

import com.a6raywa1cher.coursejournalbackend.model.AuthUser;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.jsonrestsecurity.dao.repo.IUserRepository;
import com.a6raywa1cher.jsonrestsecurity.dao.service.AbstractUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WebSecurityUserService extends AbstractUserService<AuthUser> {
    @Autowired
    public WebSecurityUserService(IUserRepository<AuthUser> authUserRepository, PasswordEncoder passwordEncoder) {
        super(authUserRepository, passwordEncoder);
    }

    @Override
    public AuthUser create(String login, String rawPassword, String role) {
        if (!"ADMIN".equals(role)) {
            throw new IllegalArgumentException("cannot create non-admin user this way");
        }
        AuthUser authUser = new AuthUser();
        LocalDateTime now = LocalDateTime.now();
        authUser.setUsername(login);
        authUser.setPassword(passwordEncoder.encode(rawPassword));
        authUser.setUserRole(UserRole.valueOf(role));
        authUser.setCreatedAt(now);
        authUser.setLastModifiedAt(now);
        return userRepository.save(authUser);
    }
}
