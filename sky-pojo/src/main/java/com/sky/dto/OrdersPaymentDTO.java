package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrdersPaymentDTO implements Serializable {
    //订单号——这个订单号来自于用户下单以后返回给前端的VO对象中——订单号我们使用时间戳生成的
    private String orderNumber;

    //付款方式
    private Integer payMethod;

}
