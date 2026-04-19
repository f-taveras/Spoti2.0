package com.spotiapp.backend.repository;

import com.spotiapp.backend.model.UserReputation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReputationRepository extends JpaRepository<UserReputation, Long> {
}
