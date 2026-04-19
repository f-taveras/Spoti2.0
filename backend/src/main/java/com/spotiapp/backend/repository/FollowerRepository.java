package com.spotiapp.backend.repository;

import com.spotiapp.backend.model.Follower;
import com.spotiapp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Long> {
    List<Follower> findByFollower(User follower);
    List<Follower> findByFollowing(User following);
    Optional<Follower> findByFollowerAndFollowing(User follower, User following);
    long countByFollower(User follower);
    long countByFollowing(User following);
}
