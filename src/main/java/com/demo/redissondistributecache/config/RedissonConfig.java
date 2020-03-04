package com.demo.redissondistributecache.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;


/**
 * Created by Tony Ng on 3/3/2020.
 */
@Configuration
public class RedissonConfig
{
	// make spring redis data, redisTemplate to use Redisson
	@Bean
	public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
		return new RedissonConnectionFactory(redisson);
	}

//	// avoid bean auto injection failed, need set @Primary
//	@Primary
//	@Bean
//	CacheManager cacheManager(RedissonClient redissonClient) {
//		return new RedissonSpringCacheManager(redissonClient, "classpath:/redisson-cache-config.yaml");
//	}

	@Bean(destroyMethod = "shutdown")
	RedissonClient redisson() throws IOException
	{
		System.out.println("RedissonSessionConfig");

		Config config = new Config();

		config.setCodec(new SerializationCodec())
				.useSentinelServers()
				.setCheckSentinelsList(false)
				.setClientName("StorefrontRedissonSessionClient")
				.setMasterName("mymaster")
				.setReadMode(ReadMode.SLAVE)
				.setSubscriptionMode(SubscriptionMode.SLAVE)
				.setDatabase(2)
				.addSentinelAddress("redis://localhost:26379");



//		Config config = Config.fromYAML(configFile.getInputStream());
		return Redisson.create(config);
	}
}