package com.spotiapp.backend.service;

import com.spotiapp.backend.model.User;
import com.spotiapp.backend.model.UserReputation;
import com.spotiapp.backend.repository.UserReputationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReputationService {

    @Autowired private UserReputationRepository userReputationRepository;

    @Transactional
    public void incrementCuratorScore(User user, int amount) {
        UserReputation rep = user.getReputation();
        if (rep == null) {
            rep = new UserReputation(user);
            user.setReputation(rep);
        }
        rep.setCuratorScore(rep.getCuratorScore() + amount);
        userReputationRepository.save(rep);
    }

    @Transactional
    public void incrementTasteScore(User user, int amount) {
        UserReputation rep = user.getReputation();
        if (rep == null) {
            rep = new UserReputation(user);
            user.setReputation(rep);
        }
        rep.setTasteScore(rep.getTasteScore() + amount);
        userReputationRepository.save(rep);
    }

    @Transactional
    public void incrementSignalScore(User user, int amount) {
        UserReputation rep = user.getReputation();
        if (rep == null) {
            rep = new UserReputation(user);
            user.setReputation(rep);
        }
        rep.setSignalScore(rep.getSignalScore() + amount);
        userReputationRepository.save(rep);
    }
}
