package com.demo.redissondistributecache.cache;

import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

	private static final Logger LOG = LoggerFactory.getLogger(DistributeCacheManager.class.getName());

	private static final String CLEAR_CACHE_PREFIX = "-clearCache-";

	private static final String EVICT_CACHE_PREFIX = "-evictCache-";

	private static final String EVICT_CACHE_SEP = "-evictCacheSEP-";

	RedisTemplate redisTemplate;

	CacheManager ehCacheCacheManager;

	@Value("${distributecache.redis.topic}")
	String topicName;

	public DistributeCacheManager(RedissonClient redisson, CacheManager ehCacheCacheManager, RedisTemplate redisTemplate)
	{
		super(redisson);
		this.ehCacheCacheManager = ehCacheCacheManager;
		this.redisTemplate = redisTemplate;
	}

	public DistributeCacheManager(RedissonClient redisson,
			Map<String, ? extends CacheConfig> config, CacheManager ehCacheCacheManager, RedisTemplate redisTemplate)
	{
		super(redisson, config);
		this.ehCacheCacheManager = ehCacheCacheManager;
		this.redisTemplate = redisTemplate;
	}

	public DistributeCacheManager(RedissonClient redisson,
			Map<String, ? extends CacheConfig> config, Codec codec, CacheManager ehCacheCacheManager, RedisTemplate redisTemplate)
	{
		super(redisson, config, codec);
		this.ehCacheCacheManager = ehCacheCacheManager;
		this.redisTemplate = redisTemplate;
	}

	public DistributeCacheManager(RedissonClient redisson, String configLocation, CacheManager ehCacheCacheManager, RedisTemplate redisTemplate)
	{
		super(redisson, configLocation);
		this.ehCacheCacheManager = ehCacheCacheManager;
		this.redisTemplate = redisTemplate;
	}

	public DistributeCacheManager(RedissonClient redisson, String configLocation, Codec codec, CacheManager ehCacheCacheManager, RedisTemplate redisTemplate)
	{
		super(redisson, configLocation, codec);
		this.ehCacheCacheManager = ehCacheCacheManager;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public Cache getCache(String name)
	{
		// TODO can create instaneMap to store object reference
		return new DistributeCache(ehCacheCacheManager, super.getCache(name), this);
	}

	public void publishClearCacheEvent(String cacheName)
	{
		publishCacheEvent(CLEAR_CACHE_PREFIX + cacheName);
	}

	public void publishEvictCacheEvent(String cacheName, Object key)
	{
		publishCacheEvent(EVICT_CACHE_PREFIX + cacheName + EVICT_CACHE_SEP + key);
	}

	public void publishCacheEvent(String eventName)
	{
		this.redisTemplate.convertAndSend(topicName, eventName);
	}

	public void receiveCacheEvent(String eventValue)
	{
		LOG.info("receive clear cache event, eventValue: {}", eventValue);

		if (eventValue.startsWith(CLEAR_CACHE_PREFIX))
		{
			String cacheName = eventValue.replace(CLEAR_CACHE_PREFIX, "");
			Cache cache = this.getCache(cacheName);
			if (cache != null)
			{
				((DistributeCache) cache).clearLocalCache();
			}
		}
		else if (eventValue.startsWith(EVICT_CACHE_PREFIX))
		{
			String cacheNameKey = eventValue.replace(EVICT_CACHE_PREFIX, "");
			String cacheName = cacheNameKey.split(EVICT_CACHE_SEP)[0];
			String cacheKey = cacheNameKey.split(EVICT_CACHE_SEP)[1];

			Cache cache = this.getCache(cacheName);
			if (cache != null)
			{
				((DistributeCache) cache).evictLocalCache(cacheKey);
			}
		}

	}
}

