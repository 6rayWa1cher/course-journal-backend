package com.a6raywa1cher.coursejournalbackend.component;

import com.a6raywa1cher.coursejournalbackend.model.AuthUser;
import com.a6raywa1cher.coursejournalbackend.model.Employee;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.jsonrestsecurity.dao.repo.IUserRepository;
import com.a6raywa1cher.jsonrestsecurity.dao.service.AbstractUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class WebSecurityUserService extends AbstractUserService<AuthUser> {
    @Autowired
    public WebSecurityUserService(IUserRepository<AuthUser> authUserRepository, PasswordEncoder passwordEncoder) {
        super(authUserRepository, passwordEncoder);
    }

    @Override
    public AuthUser create(String login, String rawPassword, String role) {
        Employee employee = new Employee();
        employee.setUsername(login);
        employee.setPassword(passwordEncoder.encode(rawPassword));
        employee.setUserRole(UserRole.valueOf(role));
        return userRepository.save(employee);
    }
}
