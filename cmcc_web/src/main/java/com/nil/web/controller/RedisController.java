package com.nil.web.controller;

import com.nil.web.bean.MapBean;
import com.nil.web.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author lianyou
 * @version 1.0
 * @date 2019/1/6 14:20
 */
@RestController
@RequestMapping(value = "/map")
public class RedisController {
  @Autowired private RedisUtils redisUtils;

  @GetMapping(value = "/list")
  public Object test2() {

    ArrayList<MapBean> list = new ArrayList<>();

    Map<Object, Object> all = redisUtils.getAllHash("D-20170412");
    for (Map.Entry<Object, Object> row : all.entrySet()) {
      MapBean map = new MapBean();
      map.setName(row.getKey().toString());
      map.setValue(Integer.parseInt(row.getValue().toString()));

      list.add(map);
    }

    return list;
  }
}
