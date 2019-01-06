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

	// kafka的相关参数
	val kafkaParams = Map[String, Object](
		"bootstrap.servers" -> broker,
		"key.deserializer" -> classOf[StringDeserializer],
		"value.deserializer" -> classOf[StringDeserializer],
		"group.id" -> groupId,
		//任务启动之前产生的数据也要读
		"auto.offset.reset" -> "earliest",
		"enable.auto.commit" -> "false"
	)

}
