package com.a6raywa1cher.coursejournalbackend.component;

import com.a6raywa1cher.coursejournalbackend.model.User;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.jsonrestsecurity.dao.repo.IUserRepository;
import com.a6raywa1cher.jsonrestsecurity.dao.service.AbstractUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class WebSecurityUserService extends AbstractUserService<User> {
    @Autowired
    public WebSecurityUserService(IUserRepository<User> userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository, passwordEncoder);
    }

    @Override
    public User create(String login, String rawPassword, String role) {
        User user = new User();
        user.setUsername(login);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setUserRole(UserRole.valueOf(role));
        return userRepository.save(user);
    }
}
