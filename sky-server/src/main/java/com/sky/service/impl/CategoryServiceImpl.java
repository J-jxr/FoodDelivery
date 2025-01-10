package com.sky.service.impl;

import com.github.pagehelper.Page; // PageHelper分页插件，处理分页数据
import com.github.pagehelper.PageHelper; // 启用分页功能
import com.sky.constant.MessageConstant; // 消息常量类
import com.sky.constant.StatusConstant; // 状态常量类
import com.sky.context.BaseContext; // 获取上下文信息的工具类（比如当前用户ID）
import com.sky.dto.CategoryDTO; // 数据传输对象，表示分类数据
import com.sky.dto.CategoryPageQueryDTO; // 用于封装分类分页查询的请求数据
import com.sky.entity.Category; // 分类实体类
import com.sky.exception.DeletionNotAllowedException; // 自定义异常类，用于抛出业务错误
import com.sky.mapper.CategoryMapper; // 分类的数据库操作接口
import com.sky.mapper.DishMapper; // 菜品的数据库操作接口
import com.sky.mapper.SetmealMapper; // 套餐的数据库操作接口
import com.sky.result.PageResult; // 分页结果封装类
import com.sky.service.CategoryService; // 分类服务接口
import lombok.extern.slf4j.Slf4j; // 日志工具
import org.springframework.beans.BeanUtils; // 属性拷贝工具
import org.springframework.beans.factory.annotation.Autowired; // 自动注入依赖
import org.springframework.stereotype.Service; // 标记这是一个服务层类

import java.time.LocalDateTime; // Java 8时间类
import java.util.List; // 列表类

/**
 * 分类业务层实现类
 */
@Service // 将该类交给Spring管理
@Slf4j // 添加日志功能
public class CategoryServiceImpl implements CategoryService {

    // 自动注入Mapper（数据库操作接口），实现与数据库的交互
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     *
     * @param categoryDTO 分类数据传输对象
     */
    public void save(CategoryDTO categoryDTO) {
        // 创建一个新的分类实体
        Category category = new Category();
        // 将categoryDTO中的数据拷贝到category对象中
        BeanUtils.copyProperties(categoryDTO, category);

        // 设置分类状态为禁用（默认值）
        category.setStatus(StatusConstant.DISABLE);

        // 设置创建时间、修改时间以及创建人、修改人（通过上下文工具类获取当前用户ID）
        //category.setCreateTime(LocalDateTime.now());
        //category.setUpdateTime(LocalDateTime.now());
        //category.setCreateUser(BaseContext.getCurrentId());
        //category.setUpdateUser(BaseContext.getCurrentId());

        // 插入分类数据到数据库
        categoryMapper.insert(category);
    }

    /**
     * 分页查询分类
     *
     * @param categoryPageQueryDTO 封装分页查询的请求数据
     * @return 分页结果
     */
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 启用分页功能，设置当前页和每页大小
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        // 调用Mapper层分页查询方法，返回分页数据
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        // 封装分页结果，包含总记录数和当前页数据
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据ID删除分类
     *
     * @param id 分类ID
     */
    public void deleteById(Long id) {
        // 检查分类是否关联了菜品，如果关联了抛出业务异常
        Integer count = dishMapper.countByCategoryId(id);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        // 检查分类是否关联了套餐，如果关联了抛出业务异常
        count = setmealMapper.countByCategoryId(id);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        // 如果没有关联菜品或套餐，则删除分类
        categoryMapper.deleteById(id);
    }

    /**
     * 修改分类
     *
     * @param categoryDTO 分类数据传输对象
     */
    public void update(CategoryDTO categoryDTO) {
        // 创建一个新的分类实体
        Category category = new Category();
        // 将传输对象中的数据拷贝到分类实体中
        BeanUtils.copyProperties(categoryDTO, category);

        // 设置修改时间和修改人
        //category.setUpdateTime(LocalDateTime.now());
        //category.setUpdateUser(BaseContext.getCurrentId());

        // 更新分类数据
        categoryMapper.update(category);
    }

    /**
     * 启用或禁用分类
     *
     * @param status 新的状态值（启用1或禁用0）
     * @param id     分类ID
     */
    public void startOrStop(Integer status, Long id) {
        // 构建一个新的分类实体，只设置需要修改的字段
        Category category = Category.builder()
                .id(id) // 设置分类ID
                .status(status) // 设置分类状态
//                .updateTime(LocalDateTime.now()) // 设置修改时间
//                .updateUser(BaseContext.getCurrentId()) // 设置修改人
                .build();
        // 更新分类状态
        categoryMapper.update(category);
    }

    /**
     * 根据类型查询分类
     *
     * @param type 分类类型
     * @return 分类列表
     */
    public List<Category> list(Integer type) {
        // 调用Mapper层方法，根据分类类型查询分类列表
        return categoryMapper.list(type);
    }
}
