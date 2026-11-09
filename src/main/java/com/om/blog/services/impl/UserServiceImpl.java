package com.om.blog.services.impl;

import com.om.blog.entities.*;
import com.om.blog.exceptions.ResourceAlreadyInUseException;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.UserDto;
import com.om.blog.repositories.AuthorSubscriptionRepo;
import com.om.blog.repositories.RoleRepo;
import com.om.blog.repositories.UserFollowRepo;
import com.om.blog.repositories.UserRepo;
import com.om.blog.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private UserFollowRepo userFollowRepo;

    @Autowired
    private AuthorSubscriptionRepo authorSubscriptionRepo;

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
        user.setRoles(new java.util.HashSet<>());
        user.setPassword(
                passwordEncoder.encode(userDto.getPassword())
        );
        Role role = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", 0));
        user.getRoles().add(role);
        User saveuser = userRepo.save(user);
        return userToDto(saveuser);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Integer userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !user.getEmail().equals(authentication.getName())) {
            throw new AccessDeniedException("You are not allowed to update this user");
        }

        if (userDto.getMobileNumber() != null && !userDto.getMobileNumber().isBlank()) {
            userRepo.findByMobileNumber(userDto.getMobileNumber()).ifPresent(existingUser -> {
                if (existingUser.getId() != userId) {
                    throw new ResourceAlreadyInUseException("Mobile number is already registered");
                }
            });
        }

        User dtouser = this.updateUserFromDto(user, userDto);
        User upUserToDto = userRepo.save(dtouser);
        return userToDto(upUserToDto);
    }

    private User updateUserFromDto(User updateUser, UserDto userDto) {
        updateUser.setName(userDto.getName());
        updateUser.setEmail(userDto.getEmail());
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            updateUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
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
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));
        return userToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepo.findAll();
        return users.stream().map(this::userToDto).toList();
    }

    @Transactional
    @Override
    public void deleteUser(Integer userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));
        userRepo.delete(user);
    }

    // Feature 2 & 6: Follow / Unfollow and Counts
    @Transactional
    @Override
    public Map<String, Object> toggleFollow(Integer authorId) {
        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", authorId));
        User currentUser = getCurrentUser();
        if (currentUser.getId() == author.getId()) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        boolean following;
        var existing = userFollowRepo.findByFollowerAndFollowing(currentUser, author);
        if (existing.isPresent()) {
            userFollowRepo.delete(existing.get());
            following = false;
        } else {
            userFollowRepo.save(new UserFollow(currentUser, author));
            following = true;
        }

        long followerCount = userFollowRepo.countByFollowing(author);
        long followingCount = userFollowRepo.countByFollower(author);

        Map<String, Object> response = new HashMap<>();
        response.put("following", following);
        response.put("followerCount", followerCount);
        response.put("followingCount", followingCount);
        return response;
    }

    @Override
    public Map<String, Object> getFollowStatus(Integer authorId) {
        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", authorId));
        User currentUser = getCurrentUserOrNull();
        boolean following = currentUser != null && userFollowRepo.existsByFollowerAndFollowing(currentUser, author);
        long followerCount = userFollowRepo.countByFollowing(author);
        long followingCount = userFollowRepo.countByFollower(author);

        Map<String, Object> response = new HashMap<>();
        response.put("following", following);
        response.put("followerCount", followerCount);
        response.put("followingCount", followingCount);
        return response;
    }

    @Override
    public List<UserDto> getFollowers(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));
        return userFollowRepo.findByFollowing(user).stream()
                .map(f -> userToDto(f.getFollower()))
                .toList();
    }

    @Override
    public List<UserDto> getFollowing(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));
        return userFollowRepo.findByFollower(user).stream()
                .map(f -> userToDto(f.getFollowing()))
                .toList();
    }

    // Feature 3: Subscribe to Authors
    @Transactional
    @Override
    public Map<String, Object> toggleSubscribe(Integer authorId) {
        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", authorId));
        User currentUser = getCurrentUser();
        if (currentUser.getId() == author.getId()) {
            throw new IllegalArgumentException("You cannot subscribe to yourself");
        }

        boolean subscribed;
        var existing = authorSubscriptionRepo.findBySubscriberAndAuthor(currentUser, author);
        if (existing.isPresent()) {
            authorSubscriptionRepo.delete(existing.get());
            subscribed = false;
        } else {
            authorSubscriptionRepo.save(new AuthorSubscription(currentUser, author));
            subscribed = true;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("subscribed", subscribed);
        return response;
    }

    @Override
    public Map<String, Object> getSubscribeStatus(Integer authorId) {
        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", authorId));
        User currentUser = getCurrentUserOrNull();
        boolean subscribed = currentUser != null && authorSubscriptionRepo.existsBySubscriberAndAuthor(currentUser, author);

        Map<String, Object> response = new HashMap<>();
        response.put("subscribed", subscribed);
        return response;
    }

    @Override
    public List<UserDto> getMySubscriptions() {
        User currentUser = getCurrentUser();
        return authorSubscriptionRepo.findBySubscriber(currentUser).stream()
                .map(s -> userToDto(s.getAuthor()))
                .toList();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadCredentialsException("User must be authenticated");
        }
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "Email", 0));
    }

    private User getCurrentUserOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return userRepo.findByEmail(auth.getName()).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private User dtoToUser(UserDto userDto) {
        return this.modelMapper.map(userDto, User.class);
    }

    private UserDto userToDto(User user) {
        UserDto userDto = this.modelMapper.map(user, UserDto.class);
        userDto.setFollowerCount(userFollowRepo.countByFollowing(user));
        userDto.setFollowingCount(userFollowRepo.countByFollower(user));

        User currentUser = getCurrentUserOrNull();
        if (currentUser != null) {
            userDto.setFollowing(userFollowRepo.existsByFollowerAndFollowing(currentUser, user));
            userDto.setSubscribed(authorSubscriptionRepo.existsBySubscriberAndAuthor(currentUser, user));
        } else {
            userDto.setFollowing(false);
            userDto.setSubscribed(false);
        }
        return userDto;
    }
}
