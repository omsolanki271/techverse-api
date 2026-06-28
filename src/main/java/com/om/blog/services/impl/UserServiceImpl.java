package com.om.blog.services.impl;

import com.om.blog.entities.User;
import com.om.blog.payloads.UserDto;
import com.om.blog.repositories.UserRepo;
import com.om.blog.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = this.dtoToUser(userDto);
        User saveuser = this.userRepo.save(user);
        return userToDto(saveuser);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Integer userId) {
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
    public void deleteUser(Integer userId) {

    }

    private User dtoToUser(UserDto user)
    {
        User user1 = new User();
        user1.setId(user.getId());
        user1.setName(user.getName());
        user1.setEmail(user.getEmail());
        user1.setPassword(user.getPassword());
        user1.setAbout(user.getAbout());
        return  user1;
    }

    private UserDto userToDto(User dto)
    {
        UserDto userDto = new UserDto();
        userDto.setId(dto.getId());
        userDto.setName(dto.getName());
        userDto.setEmail(dto.getEmail());
        userDto.setPassword(dto.getPassword());
        userDto.setAbout(dto.getAbout());
        return  userDto;
    }
}
