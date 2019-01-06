package com.nil.utils

import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.kafka010.OffsetRange
import scalikejdbc.config.DBs
import scalikejdbc.{DB, SQL}

/**
  * 每次放入Redis前需要判断偏移量，防止数据重复以及消耗资源
  *
  * @author lianyou
  * @date 2019/1/5 15:21
  * @version 1.0
  */
object OffsetManager {
	DBs.setup()

	/**
	  * 获取自己存储的偏移量信息
	  */
	def getMydbCurrentOffset: Map[TopicPartition, Long] = {
		DB.readOnly(implicit session =>
			SQL("select * from streaming_offset where groupId=?").bind(PropertiesUtil.groupId)
				.map(rs =>
					(
						new TopicPartition(rs.string("topicName"), rs.int("partitionId")),
						rs.long("offset")
					)
				).list().apply().toMap
		)
	}

	/**
	  * 持久化存储当前批次的偏移量
	  */
	def saveCurrentOffset(offsetRanges: Array[OffsetRange]) = {
		DB.localTx(implicit session => {
			offsetRanges.foreach(or => {
				SQL("replace into streaming_offset values (?,?,?,?)")
					.bind(or.topic, or.partition, or.untilOffset, PropertiesUtil.groupId)
					.update()
					.apply()
			})
		})
	}
}

