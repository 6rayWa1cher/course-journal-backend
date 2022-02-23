package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.UserDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.User;
import com.a6raywa1cher.coursejournalbackend.model.repo.UserRepository;
import com.a6raywa1cher.coursejournalbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

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
    public UserDto getById(long id) {
        return mapper.toUserDto($getById(id));
    }

    @Override
    public User getRawById(long id) {
        return $getById(id);
    }

    @Override
    public UserDto getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(mapper::toUserDto)
                .orElseThrow(() -> new NotFoundException(User.class, "username", username));
    }

    @Override
    public UserDto createUser(CreateEditUserDto dto) {
        User user = new User();

        assertUsernameAvailable(dto.getUsername());

        mapper.putUser(dto, user);

        user.setRefreshTokens(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now());
        user.setLastModifiedAt(LocalDateTime.now());

        return mapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(long id, CreateEditUserDto dto) {
        User user = $getById(id);

        assertUsernameNotChangedOrAvailable(user.getUsername(), dto.getUsername());

        mapper.putUser(dto, user);

        user.setLastModifiedAt(LocalDateTime.now());
        return mapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto patchUser(long id, CreateEditUserDto dto) {
        User user = $getById(id);
        mapper.patchUser(dto, user);
        user.setLastModifiedAt(LocalDateTime.now());
        return mapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void delete(long id) {
        User user = $getById(id);
        userRepository.delete(user);
    }

    private User $getById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(User.class, id));
    }

    private void assertUsernameNotChangedOrAvailable(String before, String now) {
        if (!Objects.equals(before, now)) {
            assertUsernameAvailable(now);
        }
    }

    private void assertUsernameAvailable(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException(User.class, "username", username);
        }
    }
}
