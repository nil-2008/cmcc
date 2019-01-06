package com.nil.utils

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.JedisPool

/**
  * Jedis使用apache commons-pool2对Jedis资源池进行管理
  *
  * @author lianyou
  * @date 2019/1/3 21:41
  * @version 1.0
  */
object RedisPoolsUtil {
	private val poolConfig = new GenericObjectPoolConfig()
	/** 连接池中最大的空闲连接数，默认是8 */
	poolConfig.setMaxIdle(5)
	/** 连接池最大的连接数，默认是8 */
	poolConfig.setMaxTotal(2000)
	/** 连接池是私有的 */
	private lazy val jedisPool = new JedisPool(poolConfig, PropertiesUtil.redisHost)

	def getJedis = {
		val jedis = jedisPool.getResource
		jedis.select(PropertiesUtil.redisIndex)
		jedis
	}
}
