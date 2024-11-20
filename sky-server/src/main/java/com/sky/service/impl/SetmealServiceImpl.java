package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐，并同时保存套餐与菜品的关联关系。
     * <p>
     * 该方法接收一个 `SetmealDTO` 对象，其中包含套餐的信息以及与套餐相关的菜品数据。方法的主要流程如下：
     * 1. 将 `SetmealDTO` 中的属性复制到 `Setmeal` 实体对象中。
     * 2. 将 `Setmeal` 实体保存到数据库中。
     * 3. 获取套餐的菜品列表，并将每个菜品的 `setmealId` 设置为刚插入的套餐 ID，确保菜品与套餐之间的关联。
     * 4. 将菜品与套餐的关联关系（`SetmealDish`）批量保存到数据库中。
     *
     * @param setmealDTO 套餐数据传输对象，包含套餐的基本信息及与套餐关联的菜品列表
     */
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        // 创建 Setmeal 实体对象，用于保存套餐的基本信息
        Setmeal setmeal = new Setmeal();

        // 将 SetmealDTO 中的属性复制到 Setmeal 实体对象中
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 保存套餐基本信息到数据库
        setmealMapper.insert(setmeal);

        // 获取套餐中的菜品列表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        // 遍历菜品列表，为每个菜品设置关联的套餐 ID
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());  // 设置套餐 ID，确保每个菜品都与当前套餐关联
        });

        // 批量保存套餐与菜品的关联关系到数据库
        setmealDishMapper.insertBatch(setmealDishes);

        // 记录日志，表示套餐和菜品的关联关系已成功保存
        log.info("套餐和菜品的关联关系保存成功");

        // 记录日志，表示套餐保存成功
        log.info("套餐保存成功");
    }


    /**
     * 分页查询套餐信息。
     * <p>
     * 该方法用于根据传入的分页查询条件，分页查询套餐数据。它使用了 PageHelper 插件来实现分页功能，并返回分页查询结果。
     * <p>
     * 方法的主要流程如下：
     * 1. 使用 `PageHelper.startPage()` 设置当前查询的页码和每页显示的数据条数。
     * 2. 调用 `setmealMapper.pageQuery()` 执行查询，并返回分页结果。
     * 3. 将查询结果封装成 `PageResult` 对象，返回给调用方。
     *
     * @param setmealPageQueryDTO 分页查询条件对象，包含查询的页码和每页显示的条数
     * @return 返回分页查询结果，包含总记录数和当前页的数据列表
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 使用 PageHelper 插件设置分页信息，getPage() 获取当前页码，getPageSize() 获取每页显示的条数
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        // 调用 setmealMapper 的分页查询方法，返回分页查询结果
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        // 记录分页查询结果的日志，便于调试和查看查询的结果
        log.info("分页查询结果：{}", page);

        // 返回封装了分页信息的结果，包含总记录数和当前页的数据列表
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐。
     * <p>
     * 该方法用于批量删除指定 ID 的套餐。在删除前，先检查每个套餐的状态，若某个套餐处于 "起售" 状态，则不允许删除。删除操作包括：
     * 1. 检查套餐的状态，若套餐正在售卖中（`ENABLE` 状态），则抛出异常，禁止删除。
     * 2. 删除套餐表中的数据。
     * 3. 删除套餐与菜品的关联关系数据。
     *
     * @param ids 套餐的 ID 列表，包含要删除的套餐 ID
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 遍历套餐 ID 列表，检查每个套餐的状态
        ids.forEach(id -> {
            // 获取套餐对象，查找该套餐的详细信息
            Setmeal setmeal = setmealMapper.getById(id);

            // 如果套餐处于 "起售" 状态，抛出异常，禁止删除
            if (StatusConstant.ENABLE == setmeal.getStatus()) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE); // 抛出自定义异常，提示套餐正在售卖中
            }
        });


        // 这里可以优化，执行批量删除数据，根据ids 进行批量删除套餐表和套餐菜品关系表中的数据。
        // 遍历套餐 ID 列表，执行删除操作
        ids.forEach(setmealId -> {
            // 删除套餐表中的数据
            setmealMapper.deleteById(setmealId);

            // 删除套餐菜品关系表中的数据，确保套餐与菜品的关联被清除
            setmealDishMapper.deleteBySetmealId(setmealId);
        });
    }


    /**
     * 修改套餐。
     * <p>
     * 该方法用于更新套餐的基本信息以及套餐与菜品的关联关系。具体操作包括：
     * 1. 修改套餐表中的基本信息（如套餐名称、价格等）。
     * 2. 删除套餐和菜品之间的旧的关联关系。
     * 3. 重新插入套餐与菜品的关联关系。
     * <p>
     * 主要步骤：
     * 1. 通过 `setmealDTO` 创建一个 `Setmeal` 对象并更新套餐表。
     * 2. 根据套餐 ID 删除旧的套餐与菜品的关联数据。
     * 3. 根据传入的套餐菜品信息，重新建立套餐与菜品的关系，并批量插入关联数据。
     *
     * @param setmealDTO 包含修改后的套餐信息和套餐菜品关系的 DTO 对象
     */
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        // 创建一个 Setmeal 对象用于封装传入的套餐信息
        Setmeal setmeal = new Setmeal();

        // 将 SetmealDTO 对象中的信息复制到 Setmeal 对象中
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 1. 修改套餐表中的信息，执行更新操作
        setmealMapper.update(setmeal);

        // 获取套餐 ID，用于后续的删除和插入操作
        Long setmealId = setmealDTO.getId();

        // 2. 删除旧的套餐与菜品的关联关系
        // 删除 `setmeal_dish` 表中与该套餐 ID 相关的记录
        setmealDishMapper.deleteBySetmealId(setmealId);

        // 获取传入的套餐菜品信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        // 遍历所有套餐菜品信息，并将套餐 ID 设置为当前套餐 ID
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        // 3. 重新插入套餐与菜品的关联关系
        // 批量插入新的套餐与菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
    }


    /**
     * 套餐的起售或停售操作。
     *
     * @param status 套餐状态，ENABLE 表示起售，DISABLE 表示停售。
     * @param id     套餐的唯一标识（ID）。
     * @throws SetmealEnableFailedException 当尝试起售套餐时，若包含未启售的菜品，会抛出此异常。
     */
    public void startOrStop(Integer status, Long id) {
        // 如果是启售操作，需要检查套餐内是否有未启售的菜品
        if (status == StatusConstant.ENABLE) {
            // 查询套餐中包含的所有菜品，使用 SQL 语句：
            // "select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?"
            List<Dish> dishList = dishMapper.getBySetmealId(id);

            // 如果查询到菜品，逐个检查其状态
            if (dishList != null && dishList.size() > 0) {
                dishList.forEach(dish -> {
                    // 如果发现有菜品状态为停售，抛出启售失败异常
                    if (StatusConstant.DISABLE == dish.getStatus()) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        // 更新套餐的状态（启售或停售）
        Setmeal setmeal = Setmeal.builder()
                .id(id)           // 套餐 ID
                .status(status)   // 套餐状态
                .build();

        // 调用数据访问层更新套餐状态
        setmealMapper.update(setmeal);
    }


    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }


    /**
     * 根据 ID 查询套餐和套餐菜品关系。
     * <p>
     * 该方法根据传入的套餐 ID，查询该套餐的基本信息以及与套餐相关的菜品信息。查询的结果会封装在 `SetmealVO` 对象中。
     * <p>
     * 主要流程：
     * 1. 通过 `setmealMapper.getById(id)` 获取指定套餐的基本信息。
     * 2. 通过 `setmealDishMapper.getBySetmealId(id)` 查询该套餐的菜品信息。
     * 3. 将查询到的套餐信息和菜品信息复制到 `SetmealVO` 对象中，返回该对象。
     *
     * @param id 套餐的 ID，用于查询指定套餐及其关联的菜品信息
     * @return 返回包含套餐信息和相关菜品信息的 `SetmealVO` 对象
     */
    public SetmealVO getByIdWithDish(Long id) {
        // 查询指定套餐的基本信息
        Setmeal setmeal = setmealMapper.getById(id);

        // 查询套餐与菜品的关联信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        // 创建 SetmealVO 对象，用于封装查询结果
        SetmealVO setmealVO = new SetmealVO();

        // 将套餐基本信息复制到 SetmealVO 对象中
        BeanUtils.copyProperties(setmeal, setmealVO);

        // 将菜品信息设置到 SetmealVO 对象中
        setmealVO.setSetmealDishes(setmealDishes);

        // 返回封装了套餐和菜品信息的 SetmealVO 对象
        return setmealVO;
    }

}
