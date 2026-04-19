package com.spotiapp.backend.repository;

import com.spotiapp.backend.model.Post;
import com.spotiapp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    long countByUser(User user);
}
