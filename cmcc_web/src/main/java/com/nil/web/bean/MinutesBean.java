package com.nil.web.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 实时显示充值金额和充值数量
 *
 * @author lianyou
 * @version 1.0
 * @date 2019/1/6 14:43
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class MinutesBean {
  private String money;
  private String count;
}
