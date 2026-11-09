package com.om.blog.repositories;

import com.om.blog.entities.User;
import com.om.blog.entities.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepo extends JpaRepository<UserFollow, Integer> {
    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);
    boolean existsByFollowerAndFollowing(User follower, User following);
    long countByFollowing(User following); // follower count
    long countByFollower(User follower);   // following count
    List<UserFollow> findByFollower(User follower);
    List<UserFollow> findByFollowing(User following);
    void deleteByFollowerAndFollowing(User follower, User following);
}

