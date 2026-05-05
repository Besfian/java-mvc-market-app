package ru.yandex.practicum.mymarket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.yandex.practicum.mymarket.model.Item;

@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public ReactiveRedisTemplate<String, Item> itemReactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {
        Jackson2JsonRedisSerializer<Item> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Item.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, Item> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, Item> context = builder
                .value(serializer)
                .hashKey(new StringRedisSerializer())
                .hashValue(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}