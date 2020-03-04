package com.demo.redissondistributecache.cache;

import com.demo.redissondistributecache.RedissonDistributeCacheApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.concurrent.Callable;


/**
 * Created by Tony Ng on 4/3/2020.
 */
public class DistributeCache implements Cache
{
	private static final Logger LOG = LoggerFactory.getLogger(DistributeCache.class.getName());

	DistributeCacheManager distributeCacheManager;

	CacheManager localCacheManager;

	Cache redisCache;

	public DistributeCache(CacheManager localCacheManager, Cache cache, DistributeCacheManager distributeCacheManager)
	{
		this.localCacheManager = localCacheManager;
		this.redisCache = cache;
		this.distributeCacheManager = distributeCacheManager;
	}

	@Override
	public String getName()
	{
		return redisCache.getName();
	}

	@Override
	public Object getNativeCache()
	{
		return redisCache.getNativeCache();
	}

	@Override
	public ValueWrapper get(Object key)
	{
		ValueWrapper wrapper = null;
		try
		{
			// get value in 1st level cache first
			wrapper = localCacheManager.getCache(redisCache.getName()).get(key);
			if (wrapper != null)
			{
				LOG.info("1st level cache found, name:{} - key: {}", redisCache.getName(), key);
				return wrapper;
			}
		}
		catch (Exception e)
		{
			throw e;
		}

		try
		{
			LOG.info("not found in 1st level cache, try in 2nd level, name:{} - key: {}", redisCache.getName(), key);

			// if 1st level not found, the get 2nd level
			wrapper = redisCache.get(key);
			if (wrapper != null)
			{
				// fill 1st level cache for faster cache for next time get
				LOG.info("found in 2nd level cache, name:{} - key: {}", redisCache.getName(), key);
				localCacheManager.getCache(redisCache.getName()).put(key, wrapper.get());

				return wrapper;
			}
		}
		catch (Exception e)
		{
			throw e;
		}

		LOG.info("not found in both 1st and 2nd level cache, name:{} - key: {}", redisCache.getName(), key);
		return null;
	}

	@Override
	public <T> T get(Object key, Class<T> type)
	{
		T value = null;
		try
		{
			// get value in 1st level cache first
			value = (T) localCacheManager.getCache(redisCache.getName()).get(key);
			if (value != null)
			{
				return value;
			}
		}
		catch (Exception e)
		{
			throw e;
		}

		try
		{
			// if 1st level not found, the get 2nd level
			value = (T) redisCache.get(key);
			if (value != null)
			{
				if (value != null)
				{
					// fill 1st level cache for faster cache for next time get
					localCacheManager.getCache(redisCache.getName()).put(key, value);
				}
				return value;
			}
		}
		catch (Exception e)
		{
			throw e;
		}

		return null;
	}

	@Override
	public <T> T get(Object key, Callable<T> valueLoader)
	{
		T value = null;
		try
		{
			// get value in 1st level cache first
			value = localCacheManager.getCache(redisCache.getName()).get(key, valueLoader);
			if (value != null)
			{
				return value;
			}
		}
		catch (Exception e)
		{
			throw e;
		}

		try
		{
			// if 1st level not found, the get 2nd level
			value = redisCache.get(key, valueLoader);
			if (value != null)
			{
				if (value != null)
				{
					// fill 1st level cache for faster cache for next time get
					localCacheManager.getCache(redisCache.getName()).put(key, value);
				}
				return value;
			}
		}
		catch (Exception e)
		{
			throw e;
		}

		return null;
	}

	@Override
	public void put(Object key, Object value)
	{
		redisCache.put(key, value);
		// TODO publish event
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value)
	{
		return redisCache.putIfAbsent(key, value);
	}

	@Override
	public void evict(Object key)
	{
		redisCache.evict(key);
		// TODO publish event
	}

	@Override
	public void clear()
	{
		redisCache.clear();
		// TODO publish event
	}

	// other node DistributeCacheManager will receive event to evict local cache by call this method
	public void evictLocalCache()
	{
		localCacheManager.getCache(redisCache.getName()).evict(redisCache.getName());
	}

	// other node DistributeCacheManager will receive event to clean local cache by call this method
	public void clearLocalCache()
	{
		localCacheManager.getCache(redisCache.getName()).clear();
	}
}
