package com.chatappauth.auth.blacklist;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlacklistRepository extends CrudRepository<BlacklistEntity, String> {

    Optional<BlacklistEntity> findByJwtToken(String jwtToken);
}
