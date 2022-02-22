//package com.a6raywa1cher.coursejournalbackend.security;
//
//import com.a6raywa1cher.coursejournalbackend.model.User;
//import com.a6raywa1cher.coursejournalbackend.model.UserRole;
//import com.a6raywa1cher.jsonrestsecurity.component.authority.GrantedAuthorityService;
//import com.a6raywa1cher.jsonrestsecurity.dao.model.IUser;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.stereotype.Component;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
//@Component
//public class GrantedAuthorityServiceImpl implements GrantedAuthorityService {
//    @Override
//    public Collection<GrantedAuthority> getAuthorities(IUser iUser) {
//        if (!(iUser instanceof User user)) throw new IllegalArgumentException();
//
//        UserRole userRole = user.getUserRole();
//
//        Set<GrantedAuthority> authoritySet = new HashSet<>();
//        authoritySet.add(new SimpleGrantedAuthority("ENABLED"));
//
//        if (userRole != null) {
//            if (UserRole.ADMIN.equals(userRole)) {
//                for (UserRole role : UserRole.values()) {
//                    authoritySet.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
//                }
//            } else {
//                authoritySet.add(new SimpleGrantedAuthority("ROLE_" + userRole));
//            }
//        }
//
//        return authoritySet;
//    }
//}
