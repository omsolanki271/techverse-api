package com.om.blog.repositories;

import com.om.blog.entities.Media;
import com.om.blog.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepo extends JpaRepository<Media , Integer> {

    List<Media> findByUser(User user);

}
