package com.demo.redissondistributecache;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@SpringBootApplication
@EnableCaching
public class RedissonDistributeCacheApplication
{
	@Autowired
	private CacheManager cacheManager;

	@Resource(name = "ehCacheManager")
	private CacheManager ehCacheCacheManager;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private RedissonClient redisson;

	private static final Logger LOG = LoggerFactory.getLogger( RedissonDistributeCacheApplication.class.getName() );

	public static void main(String[] args)
	{
		SpringApplication.run(RedissonDistributeCacheApplication.class, args);
	}

	@GetMapping("/testCache/{value}")
	@Cacheable(cacheNames = "testCache", key = "#value")
	public String testCache(@PathVariable String value)
	{
		LOG.info("when u see this, no cache yet: " + value);
		return value;
	}

	@GetMapping("/evictTestCache/{value}")
	@CacheEvict(cacheNames = "testCache", key = "#value")
	public String evictTestCache(@PathVariable String value)
	{
		LOG.info("evictTestCache: " + value);
		return value;
	}

	@GetMapping("/clearCache")
	public String clearCache()
	{
		LOG.info("clearCache: testCache ");
		cacheManager.getCache("testCache").clear();
		return "done";
	}

	@GetMapping("/checkCacheManager")
	public String checkCacheManager()
	{
		LOG.info("cacheManager: " + cacheManager);
		return null;
	}
}
