package com.om.blog.services;

import com.om.blog.entities.User;
import com.om.blog.payloads.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);
    UserDto updateUser(UserDto userDto , Integer integer);
    UserDto getUserById(Integer userid);
    List<UserDto> getAllUsers();
    void deleteUser(Integer integer);

}
