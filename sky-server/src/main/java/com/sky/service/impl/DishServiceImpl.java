package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品并保存其对应的口味信息。
     *
     * @param dishDTO 包含菜品及其口味信息的数据传输对象（DTO）。
     *                其中包括菜品基本信息和一个口味列表。
     */
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        // 创建 Dish 实体对象，用于存储菜品基本信息
        Dish dish = new Dish();
        // 将 dishDTO 中的属性值复制到 dish 对象中
        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表插入一条记录
        dishMapper.insert(dish);

        // 获取数据库生成的菜品主键 ID（insert 操作后会自动回填到 dish 对象中）
        Long dishId = dish.getId();

        // 获取菜品的口味列表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            // 如果口味列表不为空，则为每个口味对象设置菜品 ID
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId); // 将菜品的主键 ID 设置到口味对象中
            }
            // 将口味列表批量插入到口味表中
            dishFlavorMapper.insertBatch(flavors);
        }
    }


    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO 菜品分页查询条件，包含分页参数和查询条件
     * @return 返回分页查询结果，包含总记录数和当前页的记录列表
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 使用 PageHelper 开启分页功能，指定当前页码和每页显示的记录数
        // 会自动生成带有 LIMIT 和 OFFSET 的 SQL，控制数据返回的范围,
        // 省去我们自己计算当前页码以及每页展示数据，而直接使用框架提供的分页功能。
        // 同时会自动拼接到SQL中，例如：SELECT * FROM dish LIMIT 5 OFFSET 10
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        // 调用 Mapper 层方法，执行分页查询
        // 返回值是一个 Page 对象，包含当前页的数据列表以及分页元信息
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        // 封装分页结果，将总记录数和当前页数据列表传入自定义的 PageResult 对象
        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 删除菜品
     *
     * @param ids
     */
    @Override
    // 事务一致性
    @Transactional
    /*
    事务是数据库操作的一个基本概念，
    确保一组操作要么完全执行（提交），要么完全不执行（回滚）。
    @Transactional 注解在方法或类上应用，告诉 Spring 该方法或类的所有操作都应该在一个事务内执行。
     */
    public void deleteBatch(List<Long> ids) {
        // 判断当前菜品是否能够删除——是否存在起售中的菜品？
        for (Long id : ids) {
            //遍历获得所有的菜品ID
            // 通过菜品ID查询菜品信息
            Dish dish = dishMapper.getById(id);

            // 判断菜品的状态，
            // 如果状态为 "正在销售" (StatusConstant.ENABLE)，则不能删除
            if (dish.getStatus() == StatusConstant.ENABLE) {
                // 如果菜品正在销售，抛出一个业务异常，提示无法删除正在销售的菜品
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 判断菜品是否能被删除——是否存在与某个套餐有所关联？
        // 获取当前菜品关联的套餐ID列表
        List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(ids);

        // 如果套餐ID列表不为空，说明菜品与套餐有关联
        if (setmealIdsByDishIds != null && !setmealIdsByDishIds.isEmpty()) {
            // 如果菜品被套餐关联，抛出一个业务异常，提示菜品不能被删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 如果菜品可以删除，执行删除操作
        // 删除菜品表中的菜品数据
//        for (Long id : ids) {
//            // 删除菜品数据
//            dishMapper.deleteById(id);
//
//            // 删除菜品相关的口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }


        // 每for循环遍历一次，就会执行两条SQL，如果数量过多，可能就会引起一些性能上的问题。
        // sql: DELETE FROM dish WHERE id in (?, ?, ?)
        log.info("批量删除菜品， dishIds: {}", ids);
        //根据菜品ID集合批量删除菜品数据
        dishMapper.deleteByIds(ids);
        //根据菜品ID集合批量删除口味数据
        dishFlavorMapper.deleteByDishIds(ids);

    }

    /**
     * 根据菜品 ID 查询指定菜品的详细信息（包括口味信息）。
     * <p>
     * 该方法根据菜品 ID 查询数据库中的菜品信息，并从菜品的口味表中获取该菜品的所有口味数据。
     * 如果查询到对应的菜品数据，将会返回一个包含菜品信息及其口味信息的 DishVO 对象。
     *
     * @param id 菜品的唯一标识符，表示要查询的菜品的 ID
     * @return 查询结果，返回一个 DishVO 对象，包含菜品详细信息以及对应的口味信息
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 1. 先根据菜品ID查询菜品信息
        // 根据菜品 ID 从数据库中查询菜品基本信息
        Dish dish = dishMapper.getById(id);

        // 创建一个 DishVO 对象，用于封装返回的结果
        DishVO dishVO = new DishVO();

        // 如果菜品存在，则将菜品信息复制到 dishVO 中
        if (dish != null) {
            BeanUtils.copyProperties(dish, dishVO); // 将菜品基本信息复制到 dishVO

            // 2. 再根据菜品ID查询口味信息
            // 查询菜品对应的口味数据
            // 从 dish_flavor 表中根据菜品 ID 查询菜品的口味列表
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);

            // 将查询到的口味列表设置到 dishVO 的 flavors 属性中
            dishVO.setFlavors(flavors);
        }

        // 返回封装好的菜品信息和口味信息
        return dishVO;
    }


    /**
     * 更新菜品信息及其口味数据。
     * <p>
     * 该方法首先更新菜品表中的基本信息，然后根据菜品的 ID 删除原有的口味数据，
     * 接着批量插入新的口味数据，确保菜品及其口味信息的更新操作同时完成。
     *
     * @param dishDTO 包含菜品和口味信息的数据传输对象，包含了更新的菜品基本信息和口味数据
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        // 创建 Dish 对象，将 DishDTO 中的基本信息复制到 Dish 对象中
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish); // 使用 BeanUtils 复制属性

        //  1. 更新第一张表——菜品表
        // 更新菜品表中的基本信息
        dishMapper.update(dish); // 调用菜品映射器更新菜品信息

        //  2. 删除与该菜品 ID 关联的原有口味数据
        // 这一步是为了保证口味数据的一致性，防止旧数据残留
        dishFlavorMapper.deleteByDishId(dish.getId()); // 删除原有口味数据

        //  3. 获取传入的新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();

        // 如果口味数据不为空，批量插入新的口味数据
        if (flavors != null && !flavors.isEmpty()) {
            //   4. 遍历每个口味对象，设置菜品 ID，确保口味与正确的菜品关联
            //      因为前端其实并没有把菜品ID传过来，只是传入口味名称以及描述信息，所以需要手动设置菜品ID
            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId()); // 设置口味的菜品 ID
            });

            //获取到口味关联的菜品ID以后，批量插入dish_flavor表中
            //   5. 批量插入新的口味数据到 dish_flavor 表
            dishFlavorMapper.insertBatch(flavors); // 插入口味数据
        }
    }


    /**
     * 启用或禁用菜品
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {

    }

    /**
     * 根据分类 ID 查询菜品列表。
     * <p>
     * 该方法创建一个 Dish 对象作为查询条件，条件包括：
     * 1. 菜品的分类 ID 必须匹配传入的 `categoryId`。
     * 2. 菜品的状态必须是启用状态（`StatusConstant.ENABLE`）。
     * <p>
     * 然后，调用 `dishMapper.list(dish)` 方法查询符合条件的所有菜品，并返回查询结果。
     *
     * @param categoryId 菜品分类 ID，用于筛选属于指定分类的菜品
     * @return 返回符合条件的菜品列表
     */
    public List<Dish> list(Long categoryId) {
        // 使用构建器模式创建一个 Dish 对象，作为查询条件_____首先ID必须是我传入的categoryID，其次菜品必须是启售的状态
        // 该对象的分类 ID 被设置为传入的 categoryId，状态设置为启用状态
        Dish dish = Dish.builder()
                .categoryId(categoryId)  // 设置菜品的分类 ID
                .status(StatusConstant.ENABLE)  // 设置菜品的状态为启用
                .build();  // 构建一个 Dish 对象

        // 调用 dishMapper 的 list 方法，根据指定条件查询菜品并返回结果
        return dishMapper.list(dish);  // 返回符合条件的菜品列表
    }


    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    // @Override
    // public List<Dish> list(Long categoryId) {
    //     // return List.of();
    // }

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    // @Override
    // public List<DishVO> listWithFlavor(Dish dish) {
    //     // return List.of();
    // }
}
