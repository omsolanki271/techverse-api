package com.om.blog.repositories;

import com.om.blog.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepo extends JpaRepository<Comment , Integer>
{
    
}
