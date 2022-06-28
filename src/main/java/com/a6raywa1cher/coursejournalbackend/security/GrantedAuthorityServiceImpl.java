package com.a6raywa1cher.coursejournalbackend.security;

import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.model.*;
import com.a6raywa1cher.coursejournalbackend.model.repo.CourseRepository;
import com.a6raywa1cher.coursejournalbackend.model.repo.GroupRepository;
import com.a6raywa1cher.jsonrestsecurity.component.authority.GrantedAuthorityService;
import com.a6raywa1cher.jsonrestsecurity.component.checker.UserEnabledChecker;
import com.a6raywa1cher.jsonrestsecurity.dao.model.IUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.a6raywa1cher.coursejournalbackend.security.Permission.*;

@Service
public class GrantedAuthorityServiceImpl implements GrantedAuthorityService {
    private final UserEnabledChecker userEnabledChecker;
    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;

    public GrantedAuthorityServiceImpl(UserEnabledChecker userEnabledChecker, CourseRepository courseRepository, GroupRepository groupRepository) {
        this.userEnabledChecker = userEnabledChecker;
        this.courseRepository = courseRepository;
        this.groupRepository = groupRepository;
    }

    private static GrantedAuthority newAuthority(String authority) {
        return new SimpleGrantedAuthority(authority);
    }

    private Set<GrantedAuthority> getDirectAuthorities(AuthUser authUser) {
        Set<GrantedAuthority> set = new HashSet<>();
        set.add(newAuthority("ROLE_" + authUser.getUserRole()));
        set.add(newAuthority(getPermissionForAuthUser(authUser, ActionType.READ)));
        set.add(newAuthority(getPermissionForAuthUser(authUser, ActionType.WRITE)));
        switch (authUser.getUserRole()) {
            case ADMIN -> {

            }
            case TEACHER -> {
                long employeeId = authUser.getEmployee().getId();
                set.add(newAuthority(getPermissionForEmployee(employeeId, ActionType.READ)));
                set.add(newAuthority(getPermissionForEmployee(employeeId, ActionType.WRITE_CASCADE)));
            }
            case HEADMAN -> {
                long groupId = authUser.getStudent().getGroup().getId();
                Group group = getRawGroupById(groupId);
                List<Course> courses = courseRepository.findAllByGroupWithoutPage(group);
                courses.forEach(course -> set.add(newAuthority(getPermissionForCourse(course, ActionType.READ))));
                set.add(newAuthority(getPermissionForGroup(groupId, ActionType.READ)));
                set.add(newAuthority(getPermissionForGroup(groupId, ActionType.WRITE_ATTENDANCE)));
            }
            default -> throw new IllegalArgumentException();
        }
        return set;
    }

    private Set<GrantedAuthority> getCourseOwnedAuthorities(Employee employee) {
        Set<GrantedAuthority> set = new HashSet<>();
        List<Long> byOwner = courseRepository.findByOwner(employee);
        for (long id : byOwner) {
            set.add(newAuthority(getPermissionForCourse(id, ActionType.READ)));
            set.add(newAuthority(getPermissionForCourse(id, ActionType.WRITE)));
        }
        return set;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities(IUser iUser) {
        if (!AuthUser.class.isAssignableFrom(iUser.getClass())) {
            throw new IllegalArgumentException("Unknown class " + iUser.getUsername());
        }
        AuthUser authUser = (AuthUser) iUser;
        Set<GrantedAuthority> set = getDirectAuthorities(authUser);
        if (authUser.getUserRole() == UserRole.TEACHER) {
            set.addAll(getCourseOwnedAuthorities(authUser.getEmployee()));
        }
        if (userEnabledChecker.check(authUser)) {
            set.add(new SimpleGrantedAuthority("ENABLED"));
        }
        return Collections.unmodifiableSet(set);
    }

    private Group getRawGroupById(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new NotFoundException(Group.class, groupId));
    }
}
