package com.demo.redissondistributecache.config;

import com.demo.redissondistributecache.cache.DistributeCacheManager;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;


@Configuration
public class DistributeCacheConfig
{
	@Value("${distributecache.redis.topic}")
	String cacheTopicName;

	@Resource(name = "ehCacheManager")
	private CacheManager ehCacheCacheManager;

	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory)
	{
		RedisTemplate template = new RedisTemplate();

		template.setConnectionFactory(factory);

		RedisSerializer keySerializer = new StringRedisSerializer();

		template.setKeySerializer(keySerializer);
		template.setHashKeySerializer(keySerializer);

		// assume this RedisTemplate is only for redis channel, so string is enough
		template.setValueSerializer(keySerializer);

		template.afterPropertiesSet();

		return template;
	}

	// avoid bean auto injection failed, need set @Primary
	@Primary
	@Bean
	CacheManager cacheManager(RedissonClient redissonClient, RedisTemplate redisTemplate)
	{
		return new DistributeCacheManager(redissonClient, "classpath:/redisson-cache-config.yaml", ehCacheCacheManager, redisTemplate);
	}

	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic(cacheTopicName));

		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter(final DistributeCacheManager cacheManager)
	{
		return new MessageListenerAdapter(new MessageListener()
		{
			public void onMessage(Message message, byte[] pattern)
			{
				byte[] bs = message.getChannel();
				try
				{

					String topic = new String(bs, "UTF-8");
					String body = new String(message.getBody(), "UTF-8");
					cacheManager.receiveCacheEvent(body);
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();

				}
			}
		});
	}
}
