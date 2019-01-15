package com.nil.utils

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.StringDeserializer

/**
  * Typesafe的Config库，纯Java写成、零外部依赖、代码精简、功能灵活、API友好。
  * <p>支持Java properties、JSON、JSON超集格式HOCON以及环境变量。它也是Akka的配置管理库.
  *
  * @author lianyou
  * @date 2019/1/3 22:09
  * @version 1.0
  */
object PropertiesUtil {
	/**
	  * 解析application.conf配置文件
	  * 加载resource下面的配置文件，默认规则：application.conf->application.json->application.properties
	  */
	private lazy val conf: Config = ConfigFactory.load()
	//消费者ID
	val groupId = conf.getString("kafka.group.id")
	//返回订阅的主题，分隔是因为可能有多个主题
	val topic = conf.getString("kafka.topic").split(",")
	//kafka集群
	val broker = conf.getString("kafka.broker.list")

	//redis参数
	val redisHost: String = conf.getString("redis.host")
	val redisIndex: Int = conf.getInt("redis.db.index")

	//省份 code和省名称的映射关系
	import scala.collection.JavaConversions._

	val provinceCode2Name = conf.getObject("provinceCode2Name").unwrapped().toMap

	// kafka consumer配置
	val kafkaParams = Map[String, Object](
		"bootstrap.servers" -> broker,
		//现了Deserializer的key的反序列化类
		"key.deserializer" -> classOf[StringDeserializer],
		//实现了Deserializer的value的反序列化类
		"value.deserializer" -> classOf[StringDeserializer],
		"group.id" -> groupId,
		//当kafka的初始偏移量没了，或者当前的偏移量不存在的情况下，应该怎么办？下面有几种策略：
		// earliest（将偏移量自动重置为最初的值）、
		// latest（自动将偏移量置为最新的值）、
		// none（如果在消费者组中没有发现前一个偏移量，就向消费者抛出一个异常）、
		// anything else（向消费者抛出异常）
		"auto.offset.reset" -> "earliest",
		//如果设为true，消费者的偏移量会定期在后台提交。
		"enable.auto.commit" -> "false"
	)

}
