package com.linhei.queryuserid.config;

import io.lettuce.core.ReadFrom;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author linzp
 * @version 1.0.0
 * CreateDate 2020/8/18 22:26
 */
@Configuration
public class RedisConfig {

    @Bean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();

        template.setConnectionFactory(factory);
        //key序列化方式
        template.setKeySerializer(redisSerializer);
        //value序列化
        template.setValueSerializer(redisSerializer);
        //value hashmap序列化
        template.setHashValueSerializer(redisSerializer);
        //key hashmap序列化
        template.setHashKeySerializer(redisSerializer);

        return template;
    }

    /**
     * 配置redis主从分离
     * ReadFrom是配置Redis的读取策略，是一个枚举，包含：
     * MASTER：            从主节点读取
     * MASTER_PREFERRED：  优先从master节点读取，master不可用才读取replica
     * REPLICA：           从slave(replica)节点读取
     * REPLICA_PREFERRED： 优先从slave(replica)节点读取，所有slave都不可用后才读取master
     *
     * @return 配置
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer configurationBuilderCustomizer() {
        return configBuilder -> configBuilder.readFrom(ReadFrom.REPLICA_PREFERRED);
    }
}