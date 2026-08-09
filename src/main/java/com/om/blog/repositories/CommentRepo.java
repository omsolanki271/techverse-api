package com.om.blog.repositories;

import com.om.blog.entities.Comment;
import com.om.blog.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment , Integer>
{
    List<Comment> findByPost(Post post);
}
