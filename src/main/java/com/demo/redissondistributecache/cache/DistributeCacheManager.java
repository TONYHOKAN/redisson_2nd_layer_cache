package com.demo.redissondistributecache.cache;

import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;


/**
 * Created by Tony Ng on 3/3/2020.
 */
// TODO this just better to be container that no need extend RedissonSpringCacheManager?
public class DistributeCacheManager extends RedissonSpringCacheManager
{
	RedisTemplate redisTemplate;

	CacheManager ehCacheCacheManager;

	public DistributeCacheManager(RedissonClient redisson, CacheManager ehCacheCacheManager)
	{
		super(redisson);
		this.ehCacheCacheManager = ehCacheCacheManager;
	}

	public DistributeCacheManager(RedissonClient redisson,
			Map<String, ? extends CacheConfig> config, CacheManager ehCacheCacheManager)
	{
		super(redisson, config);
		this.ehCacheCacheManager = ehCacheCacheManager;
	}

	public DistributeCacheManager(RedissonClient redisson,
			Map<String, ? extends CacheConfig> config, Codec codec, CacheManager ehCacheCacheManager)
	{
		super(redisson, config, codec);
		this.ehCacheCacheManager = ehCacheCacheManager;
	}

	public DistributeCacheManager(RedissonClient redisson, String configLocation, CacheManager ehCacheCacheManager)
	{
		super(redisson, configLocation);
		this.ehCacheCacheManager = ehCacheCacheManager;
	}

	public DistributeCacheManager(RedissonClient redisson, String configLocation, Codec codec, CacheManager ehCacheCacheManager)
	{
		super(redisson, configLocation, codec);
		this.ehCacheCacheManager = ehCacheCacheManager;
	}

	@Override
	public Cache getCache(String name)
	{
		// TODO can create instaneMap to store object reference
		return new DistributeCache(ehCacheCacheManager, super.getCache(name), this);
	}
}

