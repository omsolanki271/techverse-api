package com.om.blog.controllers;


import com.om.blog.payloads.ApiResponse;
import com.om.blog.payloads.UserDto;
import com.om.blog.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    // post - create user
    @PostMapping("/")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto dto = userService.createUser(userDto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // put - update user
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto, @PathVariable("userId") Integer uid) {
        UserDto dto = userService.updateUser(userDto, uid);
        return ResponseEntity.ok(dto);
        //return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    // delete - delete user
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>(new ApiResponse("User deleted Successfully", true), HttpStatus.OK);
    }

    // get - get users
    @GetMapping("/")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        // List<UserDto> users =  userService.getAllUsers();
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    // get - get one user
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer userId)
    {
        return new ResponseEntity<>(userService.getUserById(userId),HttpStatus.OK);
    }
}
