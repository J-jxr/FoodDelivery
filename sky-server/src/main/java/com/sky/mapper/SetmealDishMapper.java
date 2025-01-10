package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询对应的套餐id
     * <p>
     * 这个方法用于根据传入的菜品ID列表，查询所有关联的套餐ID。菜品和套餐的关系是通过中间表 `setmeal_dish` 来建立的，
     * 每个菜品ID（`dish_id`）对应一个或多个套餐ID（`setmeal_id`）。
     *
     * @param dishIds 一个包含菜品ID的列表，可以是多个菜品ID（例如：[1, 2, 3, 4]）。
     *                这个列表中的菜品ID将用于查询所有相关的套餐ID。
     * @return 返回一个包含所有相关套餐ID（`setmeal_id`）的列表（`List<Long>`）。
     * 每个元素是一个与菜品ID相关联的套餐ID。
     * <p>
     * SQL 查询的示例：
     * select setmeal_id from setmeal_dish where dish_id in (1, 2, 3, 4)
     * 这里的 `dishIds` 是动态传入的，因此实际查询中的 `in` 子句会根据传入的菜品ID列表生成。
     * @see <setmeal_dish> 中间表用于存储菜品与套餐的关联关系。
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);


    /**
     * 批量保存套餐和菜品的关联关系
     *
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除套餐和菜品的关联关系
     *
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    /**
     * 根据套餐id查询套餐和菜品的关联关系
     *
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
