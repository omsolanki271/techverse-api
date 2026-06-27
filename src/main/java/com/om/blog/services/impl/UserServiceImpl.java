package com.om.blog.services.impl;

import com.om.blog.payloads.UserDto;
import com.om.blog.services.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public UserDto createUser(UserDto userDto) {
        return null;
    }

    @Override
    public UserDto updateUser(UserDto userDto, Integer integer) {
        return null;
    }

    @Override
    public UserDto getUserById(Integer userid) {
        return null;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return List.of();
    }

    @Override
    public void deleteUser(Integer integer) {

    }
}
