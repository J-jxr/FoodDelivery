package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺管理相关控制器
 * 这个类主要用来处理店铺的营业状态，比如设置店铺是“营业中”还是“打烊中”，
 * 并提供接口让外界查询店铺当前的营业状态。
 */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口") // Swagger注解，用于生成接口文档
@Slf4j // Lombok注解，用于记录日志
public class ShopController {

    /**
     * 这个常量是Redis里存储店铺营业状态的键名。
     * Redis是一种内存数据库，我们用它来保存和快速获取店铺的状态信息。
     */
    public static final String KEY = "SHOP_STATUS";

    /**
     * Redis操作模板，通过它可以对Redis里的数据进行读写操作。
     * 这里使用Spring框架提供的RedisTemplate来管理缓存数据。
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺的营业状态
     * 接收来自客户端的请求，通过Redis存储店铺是“营业中”还是“打烊中”。
     *
     * @param status 店铺状态，1表示“营业中”，0表示“打烊中”
     * @return 返回操作成功的结果
     */
    @PutMapping("/{status}")
    @ApiOperation("管理端设置店铺的营业状态") // Swagger注解，描述这个接口的功能
    public Result setStatus(@PathVariable Integer status) {
        // 打印日志，记录当前状态变化
        log.info("设置店铺的营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        // 将状态值存入Redis，键名为KEY，值为status
        redisTemplate.opsForValue().set(KEY, status);
        // 返回操作成功的信息
        return Result.success();
    }

    /**
     * 获取店铺的营业状态
     * 从Redis里读取店铺当前的营业状态，并返回给客户端。
     *
     * @return 返回店铺的营业状态，1表示“营业中”，0表示“打烊中”
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺的营业状态") // Swagger注解，描述这个接口的功能
    public Result<Integer> getStatus() {
        // 从Redis里读取店铺状态，键名为KEY
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        // 打印日志，记录当前状态
        log.info("获取店铺的营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        // 返回操作成功的信息和店铺状态
        return Result.success(status);
    }
}
