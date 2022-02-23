package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.UserDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.User;
import com.a6raywa1cher.coursejournalbackend.model.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final MapStructMapper mapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, MapStructMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<UserDto> getPage(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapper::toUserDto);
    }

    @Override
    public UserDto createUser(CreateEditUserDto dto) {
        User user = new User();
        mapper.toUser(dto, user);
        user.setRefreshTokens(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now());
        user.setLastModifiedAt(LocalDateTime.now());
        return mapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(long id, CreateEditUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id, User.class));
        mapper.toUser(dto, user);
        user.setLastModifiedAt(LocalDateTime.now());
        return mapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto patchUser(long id, CreateEditUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id, User.class));
        mapper.patchUser(dto, user);
        user.setLastModifiedAt(LocalDateTime.now());
        return mapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void delete(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id, User.class));
        userRepository.delete(user);
    }
}
