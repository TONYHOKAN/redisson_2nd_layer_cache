package com.demo.redissondistributecache.config;

import com.demo.redissondistributecache.cache.DistributeCacheManager;
import org.redisson.api.RedissonClient;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;


@Configuration
public class DistributeCacheConfig
{

	@Resource(name = "ehCacheManager")
	private CacheManager ehCacheCacheManager;

	// avoid bean auto injection failed, need set @Primary
	@Primary
	@Bean
	CacheManager cacheManager(RedissonClient redissonClient)
	{
		return new DistributeCacheManager(redissonClient, "classpath:/redisson-cache-config.yaml", ehCacheCacheManager);
	}
}
