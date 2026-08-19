package com.om.blog.services.impl;

import com.om.blog.entities.Role;
import com.om.blog.entities.User;
import com.om.blog.exceptions.ResourceAlreadyInUseException;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.UserDto;
import com.om.blog.repositories.RoleRepo;
import com.om.blog.repositories.UserRepo;
import com.om.blog.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepo roleRepo;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userDto.getMobileNumber() != null && !userDto.getMobileNumber().isBlank()) {
            if (userRepo.existsByMobileNumber(userDto.getMobileNumber())) {
                throw new ResourceAlreadyInUseException("Mobile number is already registered");
            }
        }
        User user = this.dtoToUser(userDto);
        if (user.getRoles() == null) {
            user.setRoles(new java.util.HashSet<>());
        }
        user.setPassword(
                passwordEncoder.encode(userDto.getPassword())
        );
        Role role = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", 0)
                );
        user.getRoles().add(role);
        User saveuser = userRepo.save(user);
        return userToDto(saveuser);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Integer userId) {
        User user = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User","Id",userId));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin &&
                !user.getEmail().equals(authentication.getName())) {

            throw new AccessDeniedException(
                    "You are not allowed to update this user"
            );
        }

        if (userDto.getMobileNumber() != null && !userDto.getMobileNumber().isBlank()) {
            userRepo.findByMobileNumber(userDto.getMobileNumber()).ifPresent(existingUser -> {
                if (existingUser.getId() != userId) {
                    throw new ResourceAlreadyInUseException("Mobile number is already registered");
                }
            });
        }

        User dtouser = this.updateUserFromDto(user,userDto);
        User upUserToDto = userRepo.save(dtouser);
        return userToDto(upUserToDto);
    }

    // after find update here
    private User updateUserFromDto(User updateUser, UserDto userDto) {
        updateUser.setName(userDto.getName());
        updateUser.setEmail(userDto.getEmail());
        if (userDto.getPassword() != null &&
                !userDto.getPassword().isBlank()) {
            updateUser.setPassword(
                    passwordEncoder.encode(userDto.getPassword())
            );
        }
        updateUser.setAbout(userDto.getAbout());
        updateUser.setMobileNumber(userDto.getMobileNumber());
        updateUser.setAddress(userDto.getAddress());
        updateUser.setGithubUrl(userDto.getGithubUrl());
        updateUser.setLinkedinUrl(userDto.getLinkedinUrl());
        updateUser.setInstaUrl(userDto.getInstaUrl());
        return updateUser;
    }

    @Override
    public UserDto getUserById(Integer userId) {
        User user = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User","Id",userId));
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin &&
                !user.getEmail().equals(authentication.getName())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this user"
            );
        }
        return userToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users =  userRepo.findAll();
        return users.stream().map(user -> userToDto(user)).toList();
    }

    @Override
    public void deleteUser(Integer userId) {
        User user = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User","Id",userId));
        userRepo.delete(user);
    }

    private User dtoToUser(UserDto userDto)
    {
        User user1 = this.modelMapper.map(userDto, User.class);
//        user1.setName(userDto.getName());
//        user1.setEmail(userDto.getEmail());
//        user1.setPassword(userDto.getPassword());
//        user1.setAbout(userDto.getAbout());
        return  user1;
    }

    private UserDto userToDto(User user)
    {
        UserDto userDto = this.modelMapper.map(user, UserDto.class);
//        userDto.setId(user.getId());
//        userDto.setName(user.getName());
//        userDto.setEmail(user.getEmail());
//        userDto.setPassword(user.getPassword());
//        userDto.setAbout(user.getAbout());
        return  userDto;
    }
}