package com.a6raywa1cher.coursejournalbackend.security;

import com.a6raywa1cher.coursejournalbackend.model.User;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.model.repo.CourseRepository;
import com.a6raywa1cher.jsonrestsecurity.component.authority.GrantedAuthorityService;
import com.a6raywa1cher.jsonrestsecurity.component.checker.UserEnabledChecker;
import com.a6raywa1cher.jsonrestsecurity.dao.model.IUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GrantedAuthorityServiceImpl implements GrantedAuthorityService {
    private final UserEnabledChecker userEnabledChecker;
    private final CourseRepository courseRepository;

    public GrantedAuthorityServiceImpl(UserEnabledChecker userEnabledChecker, CourseRepository courseRepository) {
        this.userEnabledChecker = userEnabledChecker;
        this.courseRepository = courseRepository;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities(IUser iUser) {
        if (!User.class.isAssignableFrom(iUser.getClass())) {
            throw new IllegalArgumentException("Unknown class " + iUser.getUsername());
        }
        User user = (User) iUser;
        Set<GrantedAuthority> set = new HashSet<>();
        set.add(new SimpleGrantedAuthority("ROLE_" + user.getUserRole()));
        set.add(new SimpleGrantedAuthority(Permission.getPermissionForUser(user, ActionType.READ)));
        set.add(new SimpleGrantedAuthority(Permission.getPermissionForUser(user, ActionType.WRITE)));
        if (user.getUserRole() == UserRole.TEACHER) {
            List<Long> byOwner = courseRepository.findByOwner(user);
            for (long id : byOwner) {
                set.add(new SimpleGrantedAuthority(Permission.getPermissionForCourse(id, ActionType.READ)));
                set.add(new SimpleGrantedAuthority(Permission.getPermissionForCourse(id, ActionType.WRITE)));
            }
        }
        if (userEnabledChecker.check(user)) {
            set.add(new SimpleGrantedAuthority("ENABLED"));
        }
        return Collections.unmodifiableSet(set);
    }
}
