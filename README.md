Simple POC about 2 layer and distribute cache integrate with SpringCache. 1st layer using EhCache and 2nd layer using Redis with Redisson as client.



Just simplly update `spring.redis.sentinel.master` in `application.properties` and test with API in `com.demo.redissondistributecache.RedissonDistributeCacheApplication` and see console log.

Test:

curl localhost:8888/testCache/1

curl  localhost:8888/evictTestCache/1

curl  localhost:8888/clearCache