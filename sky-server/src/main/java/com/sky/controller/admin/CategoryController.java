package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 分类管理控制器，处理与分类相关的后台管理功能。
 *
 * 技术亮点：
 * 1. 使用 Spring Boot 的 RESTful 风格，简洁易扩展。
 * 2. 引入 Swagger 注解自动生成接口文档。
 * 3. 结合 Lombok 的日志记录注解，方便调试。
 * 4. 分层设计，控制器仅负责接口处理，业务逻辑交由 Service 完成。
 */
@RestController
@RequestMapping("/admin/category") // 定义分类管理相关接口的基础路径
@Api(tags = "分类相关接口") // Swagger 注解，用于生成接口文档
@Slf4j // Lombok 提供的日志注解，方便打印日志信息
public class CategoryController {

    @Autowired
    private CategoryService categoryService; // 注入业务层服务对象，处理分类相关操作

    /**
     * 新增分类
     *
     * 功能：接收前端传递的分类信息，调用服务层完成保存。
     * 使用技术：
     * - @PostMapping 定义 POST 请求。
     * - @RequestBody 将请求体中的 JSON 数据映射为 Java 对象。
     *
     * @param categoryDTO 分类信息数据传输对象（DTO）
     * @return 操作成功的通用响应结果
     */
    @PostMapping
    @ApiOperation("新增分类") // Swagger 注解，描述接口功能
    public Result<String> save(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类：{}", categoryDTO); // 打印分类信息日志，便于调试
        categoryService.save(categoryDTO); // 调用业务层方法保存分类信息
        return Result.success(); // 返回操作成功结果
    }

    /**
     * 分类分页查询
     *
     * 功能：根据分页条件和查询参数，返回符合条件的分类列表。
     * 使用技术：
     * - @GetMapping 定义 GET 请求。
     * - 参数直接从查询字符串中映射到 DTO。
     *
     * @param categoryPageQueryDTO 包含分页和查询条件的对象
     * @return 分页结果，包含分类列表和分页信息
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分页查询：{}", categoryPageQueryDTO); // 打印查询条件日志
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO); // 调用业务层进行分页查询
        return Result.success(pageResult); // 返回查询结果
    }

    /**
     * 删除分类
     *
     * 功能：根据分类 ID 删除指定分类。
     * 使用技术：
     * - @DeleteMapping 定义 DELETE 请求。
     *
     * @param id 要删除的分类 ID
     * @return 操作成功的通用响应结果
     */
    @DeleteMapping
    @ApiOperation("删除分类")
    public Result<String> deleteById(Long id) {
        log.info("删除分类：{}", id); // 打印分类 ID 日志
        categoryService.deleteById(id); // 调用业务层删除分类
        return Result.success(); // 返回操作成功结果
    }

    /**
     * 修改分类
     *
     * 功能：接收前端传递的修改数据，调用服务层完成更新。
     * 使用技术：
     * - @PutMapping 定义 PUT 请求。
     * - @RequestBody 将 JSON 数据映射为 Java 对象。
     *
     * @param categoryDTO 修改后的分类信息数据传输对象
     * @return 操作成功的通用响应结果
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result<String> update(@RequestBody CategoryDTO categoryDTO) {
        categoryService.update(categoryDTO); // 调用业务层更新分类信息
        return Result.success(); // 返回操作成功结果
    }

    /**
     * 启用或禁用分类
     *
     * 功能：根据传入的状态值和分类 ID，更改分类的启用状态。
     * 使用技术：
     * - @PathVariable 从 URL 路径中提取动态参数。
     * - POST 请求，因为状态修改是操作行为，属于数据变更。
     *
     * @param status 状态值（1：启用；0：禁用）
     * @param id 分类 ID
     * @return 操作成功的通用响应结果
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id) {
        categoryService.startOrStop(status, id); // 调用业务层更改分类状态
        return Result.success(); // 返回操作成功结果
    }

    /**
     * 根据类型查询分类___是查询菜品的所有分类___还是查询套餐的所有分类
     *
     * 功能：通过分类类型，查询匹配的分类列表。
     * 使用技术：
     * - @GetMapping 定义 GET 请求。
     * - 参数直接从查询字符串中提取。
     *
     * @param type 分类类型（如 1 表示菜品分类，2 表示套餐分类）
     * @return 符合条件的分类列表
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(Integer type) {
        List<Category> list = categoryService.list(type); // 调用业务层获取分类列表
        return Result.success(list); // 返回查询结果
    }
}
