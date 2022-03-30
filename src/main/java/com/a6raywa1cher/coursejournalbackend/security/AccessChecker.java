package com.a6raywa1cher.coursejournalbackend.security;

import com.a6raywa1cher.coursejournalbackend.model.*;
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

import static com.a6raywa1cher.coursejournalbackend.security.Permission.getPermissionForCourse;
import static com.a6raywa1cher.coursejournalbackend.security.Permission.getPermissionForUser;

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

    private <T> String getPermission(T entity, ActionType type) {
        if (entity instanceof Course course) {
            return getPermissionForCourse(course, type);
        } else if (entity instanceof Task task) {
            return getPermissionForCourse(task.getCourse(), type);
        } else if (entity instanceof Student student) {
            return getPermissionForCourse(student.getCourse(), type);
        } else if (entity instanceof Criteria criteria) {
            return getPermissionForCourse(criteria.getTask().getCourse(), type);
        } else if (entity instanceof User user) {
            return getPermissionForUser(user, type);
        } else if (entity instanceof Submission submission) {
            return getPermissionForCourse(submission.getTask().getCourse(), type);
        } else if (entity instanceof CourseToken courseToken) {
            return getPermissionForCourse(courseToken.getCourse(), type);
        } else {
            throw new IllegalArgumentException("Unknown entity " + entity.getClass().getSimpleName());
        }
    }

    private boolean hasAuthority(String authority, Authentication authentication) {
        return authentication.getAuthorities()
                .stream().anyMatch(ga -> ga.getAuthority().equals(authority));
    }

    public <T> boolean hasAuthority(Long id, Class<T> clazz, String type, Authentication authentication) {
        return hasAuthority(id, clazz, ActionType.valueOf(type), authentication);
    }

    public <T> boolean hasAuthority(Long id, Class<T> clazz, ActionType type, Authentication authentication) {
        if (id == null) return false;
        if (isAdmin(authentication)) return true;
        T byId = em.find(clazz, id);
        return byId == null || hasAuthority(getPermission(byId, type), authentication);
    }

    public boolean loggedInAsOrAdmin(Long id, Authentication authentication) {
        if (isAdmin(authentication) || id == null) return true;
        Optional<User> byId = userRepository.findById(id);
        return byId.isEmpty() || loggedInAs(byId.get(), authentication);
    }

    public boolean courseTokenAuth(Authentication authentication) {
        return CourseTokenAuthentication.class.isAssignableFrom(authentication.getClass());
    }

    // ================================================================================================================

    public boolean isValidUserRoleRequest(UserRole userRole, Authentication authentication) {
        if (userRole == null) return true;
        return userRole.equals(UserRole.TEACHER) || isAdmin(authentication);
    }

    public boolean readCourseAccess(Long id, Authentication authentication) {
        return hasAuthority(id, Course.class, ActionType.READ, authentication);
    }

    public boolean editCourseAccess(Long id, Authentication authentication) {
        return hasAuthority(id, Course.class, ActionType.WRITE, authentication);
    }

    public boolean editCourseAccessWithDto(Long id, CourseRestDto dto, Authentication authentication) {
        if (isAdmin(authentication)) return true;

        Optional<Course> byId = courseRepository.findById(id);
        if (byId.isEmpty()) return true;
        Course course = byId.get();

        if (dto.getOwner() != null && !Objects.equals(dto.getOwner(), course.getOwner().getId())) {
            return false;
        }
        return hasAuthority(id, Course.class, ActionType.WRITE, authentication);
    }


    public boolean createCriteriaAccess(Long taskId, Authentication authentication) {
        return hasAuthority(taskId, Task.class, ActionType.WRITE, authentication);
    }

    public boolean readCriteriaAccess(Long id, Authentication authentication) {
        return hasAuthority(id, Criteria.class, ActionType.READ, authentication);
    }

    public boolean editCriteriaAccess(Long id, Authentication authentication) {
        return hasAuthority(id, Criteria.class, ActionType.WRITE, authentication);
    }


    public boolean createStudentAccess(Long courseId, Authentication authentication) {
        return hasAuthority(courseId, Course.class, ActionType.WRITE, authentication);
    }

    public boolean readStudentAccess(Long id, Authentication authentication) {
        return hasAuthority(id, Student.class, ActionType.READ, authentication);
    }

    public boolean editStudentAccess(Long id, Authentication authentication) {
        return hasAuthority(id, Student.class, ActionType.WRITE, authentication);
    }


    public boolean createTaskAccess(Long courseId, Authentication authentication) {
        return hasAuthority(courseId, Course.class, ActionType.WRITE, authentication);
    }

    public boolean readTaskAccess(Long id, Authentication authentication) {
        return hasAuthority(id, Task.class, ActionType.READ, authentication);
    }

    public boolean editTaskAccess(Long id, Authentication authentication) {
        return hasAuthority(id, Task.class, ActionType.WRITE, authentication);
    }


    public boolean createSubmissionAccess(Long taskId, Long studentId, Authentication authentication) {
        return hasAuthority(taskId, Task.class, ActionType.WRITE, authentication) &&
                hasAuthority(studentId, Student.class, ActionType.WRITE, authentication);
    }

    public boolean readSubmissionAccess(Long id, Authentication authentication) {
        return hasAuthority(id, Submission.class, ActionType.READ, authentication);
    }

    public boolean editSubmissionAccess(Long id, Authentication authentication) {
        return hasAuthority(id, Submission.class, ActionType.WRITE, authentication);
    }


    public boolean createCourseTokenAccess(Long courseId, Authentication authentication) {
        return hasAuthority(courseId, Course.class, ActionType.WRITE, authentication);
    }

    public boolean readCourseTokenAccess(Long id, Authentication authentication) {
        return hasAuthority(id, CourseToken.class, ActionType.READ, authentication);
    }

    public boolean editCourseTokenAccess(Long id, Authentication authentication) {
        return hasAuthority(id, CourseToken.class, ActionType.WRITE, authentication);
    }


    public boolean isUserModificationAuthorized(Long id, Authentication authentication) {
        if (isAdmin(authentication)) return true;
        Optional<User> byId = userRepository.findById(id);
        return byId.isEmpty() || loggedInAs(byId.get(), authentication);
    }

    public boolean readUserAccess(Long id, UserRole userRole, Authentication authentication) {
        return true;
    }

    public boolean editUserAccess(Long id, UserRole userRole, Authentication authentication) {
        return isUserModificationAuthorized(id, authentication) && isValidUserRoleRequest(userRole, authentication);
    }
}
