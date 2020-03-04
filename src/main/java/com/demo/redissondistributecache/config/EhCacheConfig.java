package com.demo.redissondistributecache.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;


/**
 * Created by Tony Ng on 3/3/2020.
 */
@Configuration
public class EhCacheConfig
{

	@Bean
	public CacheManager ehCacheManager()
	{
		EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
		cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
		return new EhCacheCacheManager(cacheManagerFactoryBean.getObject());
	}

}
