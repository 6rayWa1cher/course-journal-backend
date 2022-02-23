package com.a6raywa1cher.coursejournalbackend.security;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.User;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.model.repo.CourseRepository;
import com.a6raywa1cher.coursejournalbackend.model.repo.UserRepository;
import com.a6raywa1cher.jsonrestsecurity.component.resolver.AuthenticationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccessChecker {
    private final AuthenticationResolver resolver;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public AccessChecker(AuthenticationResolver resolver, UserRepository userRepository,
                         CourseRepository courseRepository) {
        this.resolver = resolver;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean loggedInAs(User user, Authentication authentication) {
        return user.equals(resolver.getUser(authentication));
    }

    public boolean isValidUserRoleRequest(UserRole userRole, Authentication authentication) {
        if (userRole == null) return true;
        return userRole.equals(UserRole.TEACHER) || isAdmin(authentication);
    }

    public boolean isUserModificationAuthorized(long id, Authentication authentication) {
        if (isAdmin(authentication)) return true;
        Optional<User> byId = userRepository.findById(id);
        return byId.isEmpty() || loggedInAs(byId.get(), authentication);
    }

    public boolean editUserAccess(long id, UserRole userRole, Authentication authentication) {
        return isUserModificationAuthorized(id, authentication) && isValidUserRoleRequest(userRole, authentication);
    }

    public boolean editCourseAccess(long id, Authentication authentication) {
        if (isAdmin(authentication)) return true;
        Optional<Course> byId = courseRepository.findById(id);
        return byId.isEmpty() || loggedInAs(byId.get().getOwner(), authentication);
    }
}
