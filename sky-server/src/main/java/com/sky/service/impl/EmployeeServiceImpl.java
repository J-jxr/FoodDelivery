package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private PageHelperAutoConfiguration pageHelperAutoConfiguration;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 后期需要进行md5加密，然后再进行比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置其余没有被赋值的属性
        //设置账号状态——默认 1表示正常，0表示锁定
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码为123456
        employee.setPassword(PasswordConstant.DEFAULT_PASSWORD);

        //设置当前记录——创建人id和修改人id
        // 后续需要改为当前登录用户的id
        //从ThreadLocal中取出当前线程的empID
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        //调用Mapper层
        employeeMapper.insert(employee);
    }

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> result = page.getResult();

        return new PageResult(total, result);

    }

    /**
     * 启用或禁用员工账号
     * 该方法根据传入的状态（启用或禁用）和员工 ID，更新员工表中的状态字段。
     *
     * @param status 员工账号的状态，1 表示启用，0 表示禁用
     * @param id     员工的唯一标识符，用于定位需要更新的员工记录
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 创建一个 Employee 对象，设置其 ID 和状态
        // 使用建造者模式创建对象：通过 id 和 status 设置员工状态
        Employee employee = Employee.builder()
                .id(id)          // 设置员工 ID
                .status(status)   // 设置员工状态（启用或禁用）
                .build();         // 构建 Employee 对象

        // 调用 employeeMapper 更新数据库中的员工状态
        // 通过传入的 Employee 对象（包含 id 和 status），执行更新操作
        // 这里的 SQL 操作是：update employee set status = ? where id = ?
        employeeMapper.update(employee);
    }

    /**
     *  根据员工 ID 查询员工信息
     * @param id
     * @return
     */
    @Override
    // 定义一个方法，根据员工 ID 查询员工信息
    public Employee getById(Long id) {
        // 调用数据访问层（Mapper）的方法，通过员工 ID 查询数据库中的员工记录
        Employee employee = employeeMapper.getById(id);

        // 出于安全考虑，将查询到的员工对象的密码字段设置为 "****"
        // 这样可以避免直接暴露敏感信息
        employee.setPassword("****");

        // 返回修改后的员工对象
        return employee;
    }


    /**
     * 修改员工信息
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置修改时间
        employee.setUpdateTime(LocalDateTime.now());
        //设置当前记录——修改人id
        // 后续需要改为当前登录用户的id
        //从ThreadLocal中取出当前线程的empID
        employee.setUpdateUser(BaseContext.getCurrentId());

        //调用Mapper层
        employeeMapper.update(employee);
    }


}
