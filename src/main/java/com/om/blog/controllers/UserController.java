package com.om.blog.controllers;

import com.om.blog.payloads.ApiResponse;
import com.om.blog.payloads.UserDto;
import com.om.blog.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // POST - create user
    @PostMapping("/")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserDto dto = userService.createUser(userDto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // PUT - update user
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto, @PathVariable("userId") Integer uid) {
        UserDto dto = userService.updateUser(userDto, uid);
        return ResponseEntity.ok(dto);
    }

    // DELETE - delete user
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>(new ApiResponse("User deleted successfully", true), HttpStatus.OK);
    }

    // GET - get all users
    @GetMapping({"", "/"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    // GET - get one user (author profile or account)
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer userId) {
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }

    // Feature 2 & 6: Toggle Follow & Count
    @PostMapping("/{authorId}/follow")
    public ResponseEntity<Map<String, Object>> toggleFollow(@PathVariable Integer authorId) {
        return ResponseEntity.ok(userService.toggleFollow(authorId));
    }

    // Get Follow Status & Counts
    @GetMapping("/{authorId}/follow-status")
    public ResponseEntity<Map<String, Object>> getFollowStatus(@PathVariable Integer authorId) {
        return ResponseEntity.ok(userService.getFollowStatus(authorId));
    }

    // Get Followers List
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserDto>> getFollowers(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getFollowers(userId));
    }

    // Get Following List
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserDto>> getFollowing(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getFollowing(userId));
    }

    // Feature 3: Toggle Subscribe
    @PostMapping("/{authorId}/subscribe")
    public ResponseEntity<Map<String, Object>> toggleSubscribe(@PathVariable Integer authorId) {
        return ResponseEntity.ok(userService.toggleSubscribe(authorId));
    }

    // Get Subscribe Status
    @GetMapping("/{authorId}/subscribe-status")
    public ResponseEntity<Map<String, Object>> getSubscribeStatus(@PathVariable Integer authorId) {
        return ResponseEntity.ok(userService.getSubscribeStatus(authorId));
    }

    // Get Current User's Subscriptions
    @GetMapping("/subscriptions/my-subscriptions")
    public ResponseEntity<List<UserDto>> getMySubscriptions() {
        return ResponseEntity.ok(userService.getMySubscriptions());
    }
}
