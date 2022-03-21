package com.chatappauth.auth.blacklist;

import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Id;
import java.io.Serializable;

@Getter
@RedisHash("Blacklist")
public class BlacklistEntity implements Serializable {

    @Id
    @Indexed
    private String id;

    @Indexed
    private String jwtToken;

    public BlacklistEntity(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}