package com.om.blog.services;

import com.om.blog.payloads.UserDto;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto updateUser(UserDto userDto , Integer userId);
    UserDto getUserById(Integer userid);
    List<UserDto> getAllUsers();
    void deleteUser(Integer userId);

    // Feature 2 & 6: Follow / Unfollow Authors & Follower Count
    Map<String, Object> toggleFollow(Integer authorId);
    Map<String, Object> getFollowStatus(Integer authorId);
    List<UserDto> getFollowers(Integer userId);
    List<UserDto> getFollowing(Integer userId);

    // Feature 3: Subscribe to Authors
    Map<String, Object> toggleSubscribe(Integer authorId);
    Map<String, Object> getSubscribeStatus(Integer authorId);
    List<UserDto> getMySubscriptions();
}
