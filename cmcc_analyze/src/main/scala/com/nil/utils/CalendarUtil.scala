package com.nil.utils

import org.apache.commons.lang3.time.FastDateFormat

/**
  * Java8的DateTimeFormatter是线程安全的，而SimpleDateFormat并不是线程安全
  *
  * @author lianyou
  * @date 2019/1/3 22:37
  * @version 1.0
  */
object CalendarUtil {
	private val format = FastDateFormat.getInstance("yyyyMMddHHmmssSSS")

	/**
	  * 计算时间差：该函数是根据本项目传输的参数的值来编写的，如果传输的值的格式不同，需要做相应的调整
	  *
	  * @param startTime
	  * @param endTime
	  * @return
	  */
	def deltTime(startTime: String, endTime: String): Long = {
		val start = startTime.substring(0, 17)
		format.parse(endTime).getTime - format.parse(start).getTime
	}
}
