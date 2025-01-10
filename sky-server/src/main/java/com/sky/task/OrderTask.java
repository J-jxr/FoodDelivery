package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;

import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
@Slf4j

public class OrderTask {


    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单的方法
     */
    /*
    @Scheduled(cron = "0 * * * * ? ")  //每分钟触发一次
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().minusMinutes(15);

        // select * from orders where status = ? and order_time < (当前时间 - 15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,time);

        if(!ordersList.isEmpty()){
            //采用stream流的方式，来创建一个新的updateList用于执行update操作。
            List<Orders> updateList = ordersList.stream().map(x -> {
                Orders orders = new Orders();
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消！");
                orders.setCancelTime(LocalDateTime.now());
                orders.setId(x.getId());
                return orders;
            }).collect(Collectors.toList());

            //批量处理数据，效率更好
            orderMapper.batchUpdateOrders(updateList);
            log.info("共处理了{}个超时支付订单", updateList.size());

        }else{
            log.info("没有超时订单需要处理");
        }
    }

    */


    /**
     * 处理一直处于“派送中”状态的订单
     */

    /*
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("处理派送中订单：{}", LocalDateTime.now());
        // select * from orders where status = 4 and order_time < 当前时间-1小时
        LocalDateTime time = LocalDateTime.now().minusMinutes(60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if(ordersList != null && ordersList.size() > 0){
            ordersList.forEach(order -> {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            });
        }
    }


     */













}
