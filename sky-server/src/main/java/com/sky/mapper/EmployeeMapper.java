package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据主键动态修改员工属性
     * 通过传入的 Employee 对象，动态更新员工的属性。该方法使用 MyBatis 的映射文件来编写 SQL 语句。
     * 这里的 SQL 是根据传入的 `employee` 对象中非空字段动态更新对应的数据库字段。
     *
     * @param employee 包含需要更新的员工属性和主键的 Employee 对象
     *                 其中，员工 ID 会作为主键进行定位，其他非空属性将作为更新的字段。
     */
    void update(Employee employee);


    /**
     * 根据用户名查询员工
     *
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);


    /**
     * 新增员工
     *
     * @param employee
     */
    @Insert("insert into employee (name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user)" +
            "values" +
            "(#{name},#{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);


    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);
}
