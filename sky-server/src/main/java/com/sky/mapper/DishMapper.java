package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据菜品ID集合批量删除dish
     *
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入菜品数据
     *
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);


    List<Dish> list(Dish dish);


    /**
     * 根据套餐 ID 查询对应的菜品列表。
     *
     * @param setmealId 套餐的唯一标识（ID）。
     * @return 包含该套餐中所有菜品的列表，如果套餐未关联任何菜品，返回空列表。
     * <p>
     * SQL 说明：
     * - 查询语句为：
     * "SELECT a.* FROM dish a
     * LEFT JOIN setmeal_dish b ON a.id = b.dish_id
     * WHERE b.setmeal_id = #{setmealId}"
     * - 通过 `setmeal_dish` 表将 `dish` 和 `setmeal` 进行关联。
     * - `a.*` 表示查询菜品表的所有字段。
     * - 仅查询与指定 `setmealId` 相关联的菜品。
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

}
