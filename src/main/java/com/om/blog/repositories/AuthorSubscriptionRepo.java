package com.om.blog.repositories;

import com.om.blog.entities.AuthorSubscription;
import com.om.blog.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorSubscriptionRepo extends JpaRepository<AuthorSubscription, Integer> {
    Optional<AuthorSubscription> findBySubscriberAndAuthor(User subscriber, User author);
    boolean existsBySubscriberAndAuthor(User subscriber, User author);
    long countByAuthor(User author);
    List<AuthorSubscription> findBySubscriber(User subscriber);
    void deleteBySubscriberAndAuthor(User subscriber, User author);
}

