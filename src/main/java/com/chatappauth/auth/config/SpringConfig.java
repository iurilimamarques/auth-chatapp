package com.chatappauth.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;

@Configuration
public class SpringConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.database}")
    private int database;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.url}")
    private String url;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() throws URISyntaxException {
        String environment = System.getenv("ENVIRONMENT");

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        if (Objects.isNull(environment) || !environment.equals("prod")) {
            redisStandaloneConfiguration.setHostName(host);
            redisStandaloneConfiguration.setPort(port);
            redisStandaloneConfiguration.setDatabase(database);
            redisStandaloneConfiguration.setPassword(RedisPassword.none());
        } else {
            URI redisURI = new URI(url);
            redisStandaloneConfiguration.setHostName(redisURI.getHost());
            redisStandaloneConfiguration.setPort(redisURI.getPort());
            redisStandaloneConfiguration.setDatabase(database);
            redisStandaloneConfiguration.setPassword(redisURI.getUserInfo().split(":", 2)[1]);
        }

        JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfigurationBuilder = JedisClientConfiguration.builder();
        jedisClientConfigurationBuilder.connectTimeout(Duration.ofSeconds(60));
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfigurationBuilder.build());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() throws URISyntaxException {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}
