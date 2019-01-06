package com.nil.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lianyou
 * @version 1.0
 * @date 2019/1/4 22:38
 */
@Controller
public class ThymeleafController {
  @RequestMapping(value = "/index")
  public Object index(ModelMap map) {
    map.addAttribute("name", "thymeleaf-nil");
    return "thymeleaf/index";
  }

  @RequestMapping(value = "/map")
  public Object map(ModelMap map) {
    map.addAttribute("name", "thymeleaf-nil");
    return "thymeleaf/map";
  }
}
