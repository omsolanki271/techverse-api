package com.om.blog.services.impl;

import com.om.blog.entities.User;
import com.om.blog.exceptions.ResourceNotFoundException;
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
        User saveuser = userRepo.save(user);
        return userToDto(saveuser);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Integer userId) {
        User user = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User","Id",userId));
        User dtouser = this.updateUserFromDto(user,userDto);
        User upUserToDto = userRepo.save(dtouser);
        return userToDto(upUserToDto);
    }

    private User updateUserFromDto(User updateUser, UserDto userDto) {
        updateUser.setName(userDto.getName());
        updateUser.setEmail(userDto.getEmail());
        updateUser.setPassword(userDto.getPassword());
        updateUser.setAbout(userDto.getAbout());
        return updateUser;
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

    private User dtoToUser(UserDto userDto)
    {
        User user1 = new User();
        user1.setId(userDto.getId());
        user1.setName(userDto.getName());
        user1.setEmail(userDto.getEmail());
        user1.setPassword(userDto.getPassword());
        user1.setAbout(userDto.getAbout());
        return  user1;
    }

    private UserDto userToDto(User user)
    {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());
        userDto.setAbout(user.getAbout());
        return  userDto;
    }
}
