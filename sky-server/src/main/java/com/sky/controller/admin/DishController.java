package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理相关接口控制器。
 * 提供菜品的新增、删除、查询、启停用、更新等功能。
 */
@RestController // 表示该类是一个控制器，并且每个方法返回的对象会自动序列化为 JSON 格式
@RequestMapping("/admin/dish") // 定义该控制器的基础访问路径
@Api(tags = "菜品相关接口") // 用于 Swagger 文档生成，描述接口信息
@Slf4j // 使用 Lombok 提供的日志功能，方便记录日志信息
public class DishController {

    @Autowired // 自动注入菜品业务服务对象
    private DishService dishService;

    @Autowired // 自动注入菜品数据访问对象
    private DishMapper dishMapper;

    @Autowired // 自动注入 Redis 操作模板
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品。
     *
     * @param dishDTO 菜品数据传输对象，包含菜品信息
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("新增菜品") // Swagger 注解，描述该接口的功能
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO); // 记录日志
        dishService.saveWithFlavor(dishDTO); // 调用服务层保存菜品和其口味信息

        String key = "dish_" + dishDTO.getCategoryId();
        clearCache(key);


        return Result.success(); // 返回成功结果
    }

    /**
     * 菜品分页查询。
     *
     * @param dishPageQueryDTO 菜品分页查询参数
     * @return 分页查询结果
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    /*
    方法的参数类型如果是一个 JavaBean（即一个普通的对象，比如 DishPageQueryDTO），
    并且这个对象的属性与请求参数的名称匹配，Spring 会自动将请求参数绑定到该对象的相应字段。
    DishPageQueryDTO 对象本身就能通过 Spring 的数据绑定机制（基于 JavaBean 的 getter/setter 方法）来自动填充请求参数，
    所以你不需要显式地使用 @RequestParam 注解。
     */
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO); // 记录日志
        dishPageQueryDTO.setPageSize(100000); // 设置每页记录数为 100000，暂时无分页限制
        return Result.success(dishService.pageQuery(dishPageQueryDTO)); // 调用服务层方法获取分页结果
    }

    /**
     * 启用或停用菜品。
     *
     * @param status 状态，1 表示启用，0 表示停用
     * @param id     菜品 ID
     * @return 操作结果
     */
    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用或停用菜品：{}", id); // 记录日志
        dishService.startOrStop(status, id); // 调用服务层方法启用或停用菜品
        clearCache("dish_*"); // 清理 Redis 缓存中与菜品相关的所有数据
        return Result.success(); // 返回成功结果
    }

    /**
     * 批量删除菜品。
     *
     * @param ids 要删除的菜品 ID 列表
     * @return 操作结果
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    /*
    @RequestParam 的作用是从 HTTP 请求中获取名为 ids 的参数（可能是多个菜品 ID），
    并将这些值绑定到方法的 Long[] ids 参数上，进行批量删除操作
     */
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}", ids);
        dishService.deleteBatch(ids); // 调用服务层方法批量删除菜品

        // 将所有的菜品缓存数据清理掉，所有以dish_开头的key
        // 获取所有符合模式 "dish_*" 的键（即以 "dish_" 开头的所有键）
        Set<String> keys = redisTemplate.keys("dish_*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }

        return Result.success(); // 返回成功结果
    }

    /**
     * 根据菜品 ID 查询指定菜品的详细信息（包括口味）。
     * <p>
     * 该方法根据传入的菜品 ID 查询数据库中的菜品详细信息，返回的结果不仅包括菜品本身的信息，
     * 还包含与该菜品相关联的口味信息。
     *
     * @param id 菜品 ID，唯一标识一个菜品
     * @return 查询结果，包含菜品详细信息及其关联的口味信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询指定菜品")
    public Result<DishVO> getByIdWithFlavor(@PathVariable Long id) {  //路径参数+@PathVariable
        log.info("根据ID查询指定菜品：{}", id); // 记录查询菜品的 ID 日志
        return Result.success(dishService.getByIdWithFlavor(id)); // 调用服务层方法查询菜品详细信息，并返回成功结果
    }

    /**
     * 更新菜品信息，包括菜品本身的信息和菜品的口味信息。
     * <p>
     * 该方法用于更新菜品的信息，首先会删除与该分类相关的 Redis 缓存数据，
     * 然后更新数据库中的菜品及其口味信息。更新操作完成后，会返回一个操作成功的结果。
     *
     * @param dishDTO 菜品数据传输对象，包含菜品的基本信息和口味信息
     * @return 操作结果，表示更新操作是否成功
     */
    @PutMapping
    @ApiOperation("更新菜品信息")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("更新菜品信息：{}", dishDTO); // 记录更新的菜品信息日志

        // 将所有的菜品缓存数据清理掉，所有以dish_开头的key
        // 获取所有符合模式 "dish_*" 的键（即以 "dish_" 开头的所有键）
        Set<String> keys = redisTemplate.keys("dish_*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }

        /*
        clearCache("dish_*");
         */

        dishService.updateWithFlavor(dishDTO); // 调用服务层方法更新菜品及口味信息
        return Result.success(); // 返回成功结果
    }


    /**
     * 根据分类 ID 查询菜品列表。
     *
     * @param categoryId 分类 ID
     * @return 菜品列表
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> dishList = dishService.list(categoryId); // 调用服务层方法获取菜品列表
        return Result.success(dishList); // 返回查询结果
    }

    /**
     * 统一清理 Redis 缓存中与菜品相关的所有数据。
     *
     * @param
     */
    private void clearCache(String pattern) {
        Set<String> cacheKeys = redisTemplate.keys(pattern); // 获取匹配的 Redis 缓存键集合
        redisTemplate.delete(cacheKeys); // 删除对应的缓存数据
    }
}
