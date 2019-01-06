package com.nil.runner

import com.nil.utils.{KPIUtil, OffsetManager, PropertiesUtil}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * CMCC实时充值监控平台项目
  *
  * @author lianyou
  * @date 2019/1/3 22:50
  * @version 1.0
  */
object AppMain {
	def main(args: Array[String]): Unit = {
		//日志过滤
		Logger.getLogger("org.apacje.spark").setLevel(Level.OFF)

		val sparkConf = new SparkConf()
		sparkConf.setAppName("CMCC实时充值监控平台项目")
		//本地运行
		//如果在集群上运行的话，需要去掉：sparkConf.setMaster("local[*]")
		sparkConf.setMaster("local[*]")

		//RDD序列化 节约内存
		sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
		//rdd压缩
		sparkConf.set("spark.rdd.compress", "true")
		//设置每次拉取的数量，为了防止一下子拉取的数据过多，系统处理不过来,这里并不是拉取100条，是有公式的。
		//batchSize = partitionNum * 分区数量 * 采样时间
		sparkConf.set("spark.streaming.kafka.maxRatePerPartition", "1000")
		//设置优雅的结束，这样可以避免数据的丢失
		sparkConf.set("spark.streaming.stopGracefullyOnShutdown", "true")

		//创建 SparkStreaming
		val ssc = new StreamingContext(sparkConf, Seconds(2))

		//提取数据库中存储的偏移量
		val currentOffset = OffsetManager.getMydbCurrentOffset

		//使用广播的方式匹配省份
		val provinceCode2Name = ssc.sparkContext.broadcast(PropertiesUtil.provinceCode2Name)

		/*
		  * 创建直接冲kafka中读取数据的对象
		  *
		  * LocationStrategies：位置策略，如果kafka的broker节点跟Executor在同一台机器上给一种策略，不在一台机器上给另外一种策略
		  * 设定策略后会以最优的策略进行获取数据
		  * 一般在企业中kafka节点跟Executor不会放到一台机器的，原因是kakfa是消息存储的，Executor用来做消息的计算，
		  * 因此计算与存储分开，存储对磁盘要求高，计算对内存、CPU要求高
		  * 如果Executor节点跟Broker节点在一起的话使用PreferBrokers策略，如果不在一起的话使用PreferConsistent策略
		  * 使用PreferConsistent策略的话，将来在kafka中拉取了数据以后尽量将数据分散到所有的Executor上
		  */

		val stream = KafkaUtils.createDirectStream(ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](PropertiesUtil.topic, PropertiesUtil.kafkaParams, currentOffset))

		//数据处理
		stream.foreachRDD(rdd => {
			val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

			val baseData = KPIUtil.baseDataRDD(rdd)

			//计算业务概况
			KPIUtil.Kpi_general(baseData)
			KPIUtil.kpi_general_hour(baseData)

			//业务质量
			KPIUtil.kpi_quality(baseData, provinceCode2Name)

			//实时充值情况分析
			KPIUtil.kpi_realtime_minute(baseData)

			//存储偏移量
			OffsetManager.saveCurrentOffset(offsetRanges)
		})
		ssc.start()
		ssc.awaitTermination()
	}
}
