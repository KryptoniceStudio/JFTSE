package com.jftse.entities.database.repository.challenge;

import com.jftse.entities.database.model.challenge.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    Optional<Challenge> findChallengeByChallengeIndex(Integer challengeIndex);
}
