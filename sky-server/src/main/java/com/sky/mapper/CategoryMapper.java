package com.sky.mapper;

import com.github.pagehelper.Page; // PageHelper分页插件，处理分页数据
import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType; // 操作类型枚举（但这个在当前代码中并未使用）
import com.sky.dto.CategoryPageQueryDTO; // 分类分页查询的DTO（数据传输对象）
import com.sky.entity.Category; // 分类实体类
import org.apache.ibatis.annotations.Delete; // MyBatis的删除注解
import org.apache.ibatis.annotations.Insert; // MyBatis的插入注解
import org.apache.ibatis.annotations.Mapper; // 标记这个接口是MyBatis的Mapper
import java.util.List; // 列表类

@Mapper // 标记这是MyBatis的Mapper接口
public interface CategoryMapper {

    /**
     * 插入分类数据
     * @param category 分类实体，包含要插入的字段
     */
    @Insert("insert into category(type, name, sort, status, create_time, update_time, create_user, update_user)" +
            " VALUES" +
            " (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    // 使用MyBatis的@Insert注解执行SQL插入操作
    @AutoFill(value = OperationType.INSERT)
    void insert(Category category);

    /**
     * 分页查询分类
     * @param categoryPageQueryDTO 分类分页查询的DTO，包含分页参数和查询条件
     * @return 分页结果，包含分类数据
     */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
    // PageHelper插件会自动处理分页查询逻辑

    /**
     * 根据ID删除分类
     * @param id 分类的ID
     */
    @Delete("delete from category where id = #{id}")
    // 使用MyBatis的@Delete注解执行删除操作
    void deleteById(Long id);

    /**
     * 更新分类信息
     * @param category 分类实体，包含要更新的字段
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);
    // 由于没有使用注解，MyBatis会根据接口方法和XML映射文件执行相应的SQL更新操作

    /**
     * 根据分类类型查询分类列表
     * @param type 分类类型（例如：1表示某类、2表示另一类）
     * @return 分类列表，包含所有匹配的分类数据
     */
    List<Category> list(Integer type);
    // 通过查询条件（类型）来获取对应的分类数据
}
