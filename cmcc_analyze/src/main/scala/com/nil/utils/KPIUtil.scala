package com.nil.utils

import com.alibaba.fastjson.JSON
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

/**
  * 指标计算
  *
  * @author lianyou
  * @date 2019/1/4 16:44
  * @version 1.0
  */
object KPIUtil {
	/**
	  * 整理基础数据
	  */

	def baseDataRDD(rdd: RDD[ConsumerRecord[String, String]]): RDD[(String, String, List[Double], String, String)] = {
		// ConsumerRecord => JSONObject
		rdd.map(cr => JSON.parseObject(cr.value()))
			// 过滤出充值通知日志
			.filter(obj => obj.getString("serviceName").equalsIgnoreCase("reChargeNotifyReq"))
			.map(obj => {
				// 判断该条日志是否是充值成功的日志
				val result = obj.getString("bussinessRst")
				val fee = obj.getDouble("chargefee")

				// 充值发起时间和结束时间
				val requestId = obj.getString("requestId")
				// 数据当前日期
				val day = requestId.substring(0, 8)
				val hour = requestId.substring(8, 10)
				val minute = requestId.substring(10, 12)
				val receiveTime = obj.getString("receiveNotifyTime")

				//省份Code
				val provinceCode = obj.getString("provinceCode")
				val costTime = CalendarUtil.deltTime(requestId, receiveTime)
				val succAndFeeAndTime: (Double, Double, Double) =
					if (result.equals("0000")) (1, fee, costTime)
					else (0, 0, 0)

				// [日期, 小时, Kpi(订单，成功订单，订单金额，订单时长),省份Code，分钟数]
				(day, hour, List[Double](1, succAndFeeAndTime._1, succAndFeeAndTime._2, succAndFeeAndTime._3), provinceCode, minute)
			}).cache()
	}

	/**
	  * 业务概况（总订单量、成功订单、总金额、话费时间）
	  *
	  * @param baseData
	  */
	def Kpi_general(baseData: RDD[(String, String, List[Double], String, String)]): Unit = {
		baseData.map(tp => (tp._1, tp._3))
			.reduceByKey((list1, list2) => {
				//将所有的元素链为一个列表之后进行相加运算
				list1.zip(list2).map(tp => tp._1 + tp._2)
			})
			.foreachPartition(partition => {
				val jedis = RedisPoolsUtil.getJedis
				partition.foreach(tp => {
					jedis.hincrBy("A-" + tp._1, "total", tp._2(0).toLong)
					jedis.hincrBy("A-" + tp._1, "succ", tp._2(1).toLong)
					jedis.hincrByFloat("A-" + tp._1, "money", tp._2(2))
					jedis.hincrBy("A-" + tp._1, "cost", tp._2(3).toLong)
					// key的有效期
					jedis.expire("A-" + tp._1, 48 * 60 * 60)
				})
				jedis.close()
			})
	}

	/**
	  * 业务概述：每小时的充值总订单量，每小时的成功订单量
	  * 日期、时间、LIST（总订单量、成功订单量、充值成功总金额、时长）、
	  *
	  * @param baseData
	  */
	def kpi_general_hour(baseData: RDD[(String, String, List[Double], String, String)]): Unit = {
		//实时充值业务
		baseData.map(tp => ((tp._1, tp._2), List(tp._3(0), tp._3(1))))
			.reduceByKey((list1, list2) => {
				list1.zip(list2).map(tp => tp._1 + tp._2)
			})
			.foreachPartition(partition => {
				val jedis = RedisPoolsUtil.getJedis
				partition.foreach(tp => {
					jedis.hincrBy("B-" + tp._1._1, "T" + tp._1._2, tp._2(0).toLong)
					jedis.hincrBy("B-" + tp._1._1, "S" + tp._1._2, tp._2(1).toLong)
					// key的有效期
					jedis.expire("B-" + tp._1._1, 48 * 60 * 60)
				})
				jedis.close()
			})
	}

	/**
	  * 业务质量 总的充值成功订单量，以省为单位
	  *
	  * @param baseData
	  * @param pcode2pname
	  */
	def kpi_quality(baseData: RDD[(String, String, List[Double], String, String)], pcode2pname: Broadcast[Map[String, AnyRef]]): Unit = {
		//  全国业务失败量
		// (日期, 小时, Kpi(订单，成功订单，订单金额，订单时长))
		baseData.map(tp => ((tp._1, tp._4), tp._3(1)))
			.reduceByKey(_ + _)
			.foreachPartition(partition => {
				val jedis = RedisPoolsUtil.getJedis
				partition.foreach(tp => {
					jedis.hincrBy("C-" + tp._1._1, pcode2pname.value.getOrElse(tp._1._2, tp._1._2).toString, tp._2.toLong)
					// key的有效期
					jedis.expire("C-" + tp._1._1, 48 * 60 * 60)
				})
				jedis.close()
			})
	}

	/**
	  * (日期, 小时, Kpi(订单，成功订单，订单金额，订单时长),省份Code，分钟数)
	  * 实时统计每分钟的充值金额和订单量
	  *
	  * @param baseData
	  */
	def kpi_realtime_minute(baseData: RDD[(String, String, List[Double], String, String)]): Unit = {
		baseData.map(tp => ((tp._1, tp._2, tp._5), List(tp._3(1), tp._3(2))))
			.reduceByKey((list1, list2) => {
				list1.zip(list2).map(tp => tp._1 + tp._2)
			})
			.foreachPartition(partition => {
				val jedis = RedisPoolsUtil.getJedis
				partition.foreach(tp => {
					//订单数
					jedis.hincrBy("D-" + tp._1._1, "province" + tp._1._2 + tp._1._3, tp._2(0).toLong)
					//每分钟的成交金额
					jedis.hincrBy("D-" + tp._1._1, "salary" + tp._1._2 + tp._1._3, tp._2(1).toLong)
					// key的有效期
					jedis.expire("D-" + tp._1._1, 48 * 60 * 60)
				})
				jedis.close()
			})
	}

}