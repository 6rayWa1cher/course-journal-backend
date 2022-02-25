package com.a6raywa1cher.coursejournalbackend.security;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.User;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.model.repo.CourseRepository;
import com.a6raywa1cher.coursejournalbackend.model.repo.UserRepository;
import com.a6raywa1cher.coursejournalbackend.rest.dto.CourseRestDto;
import com.a6raywa1cher.jsonrestsecurity.component.resolver.AuthenticationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.Objects;
import java.util.Optional;

@Component
public class AccessChecker {
    private final AuthenticationResolver resolver;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EntityManager em;

    @Autowired
    public AccessChecker(AuthenticationResolver resolver, UserRepository userRepository,
                         CourseRepository courseRepository, EntityManager em) {
        this.resolver = resolver;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.em = em;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean loggedInAs(User user, Authentication authentication) {
        return user.equals(resolver.getUser(authentication));
    }

    public boolean loggedInAs(Long id, Authentication authentication) {
        return Optional.ofNullable(id)
                .flatMap(userRepository::findById)
                .map(u -> u.equals(resolver.getUser(authentication)))
                .orElse(true);
    }

    public <T extends Owned> boolean isOwnedByClientOrAdmin(long id, Class<T> ownedClass, Authentication authentication) {
        if (isAdmin(authentication)) return true;
        T byId = em.find(ownedClass, id);
        return byId == null || loggedInAs(byId.getOwnerId(), authentication);
    }

    public boolean loggedInAsOrAdmin(Long id, Authentication authentication) {
        if (isAdmin(authentication) || id == null) return true;
        Optional<User> byId = userRepository.findById(id);
        return byId.isEmpty() || loggedInAs(byId.get(), authentication);
    }

    // ================================================================================================================

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

    public boolean editCourseAccess(long id, CourseRestDto dto, Authentication authentication) {
        if (isAdmin(authentication)) return true;

        Optional<Course> byId = courseRepository.findById(id);
        if (byId.isEmpty()) return true;
        Course course = byId.get();

        if (dto.getOwner() != null && !Objects.equals(dto.getOwner(), course.getOwner().getId())) {
            return false;
        }

        return loggedInAs(course.getOwner(), authentication);
    }

    public boolean deleteCourseAccess(long id, Authentication authentication) {
        return isOwnedByClientOrAdmin(id, Course.class, authentication);
    }

}
