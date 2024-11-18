package com.sky.aspect;

import com.sky.annotation.AutoFill; // 自定义注解，用于标记需要自动填充的字段
import com.sky.constant.AutoFillConstant; // 包含字段名称的常量类
import com.sky.context.BaseContext; // 用于获取当前线程中的用户信息
import com.sky.enumeration.OperationType; // 数据库操作类型枚举类（INSERT 或 UPDATE）
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint; // AOP中的连接点对象，表示被拦截的方法
import org.aspectj.lang.annotation.Aspect; // 表示这是一个AOP切面类
import org.aspectj.lang.annotation.Before; // 前置通知，在方法执行前触发
import org.aspectj.lang.annotation.Pointcut; // 定义切入点
import org.aspectj.lang.reflect.MethodSignature; // 方法签名，包含方法的详细信息
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException; // 反射异常
import java.lang.reflect.Method; // Java反射中的方法对象
import java.time.LocalDateTime; // 用于获取当前时间

/**
 * 自动填充切面类，用于处理标记了@AutoFill注解的方法
 * 在执行这些方法之前，自动进行公共字段的填充（如创建时间、更新时间等）。
 */
@Aspect  // 这个注解表示这是一个切面类，用于定义横切关注点
@Component  // 将该类交给Spring容器管理，使其成为一个Spring Bean
@Slf4j  // 使用@Slf4j生成日志对象，便于记录日志
public class AutoFillAspect {
    /**
     * 定义切入点：拦截所有标注了@AutoFill注解的方法
     * execution(* com.sky.mapper.*.*(..)) 表示拦截com.sky.mapper包下的所有方法
     *
     * @annotation(com.sky.annotation.AutoFill) 表示仅拦截被@AutoFill注解标注的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    } // 切入点方法，定义了需要拦截哪些方法

    /**
     * 前置通知：在匹配的目标方法执行前执行
     *
     * @param joinPoint 通过JoinPoint可以获取目标方法的信息（例如方法名称、参数等）
     */
    @Before("autoFillPointCut()")
    // 在autoFillPointcut定义的切入点方法执行之前执行此方法
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段自动填充"); // 记录日志

        // 1. 获取被拦截方法的签名对象
        // 1. 获取当前被拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); // 获取方法上的@AutoFill注解
        OperationType operationType = autoFill.value(); // 从注解中获取操作类型（INSERT 或 UPDATE）

        // 2. 获取被拦截方法的参数列表——实体对象
        Object[] args = joinPoint.getArgs(); // 获取目标方法的所有参数
        if (args.length == 0) {
            // 如果没有参数，直接返回
            return;
        }

        Object entity = args[0]; // 约定第一个参数是需要操作的实体对象

        // 3. 准备要赋值的数据
        LocalDateTime now = LocalDateTime.now(); // 获取当前时间
        Long currentId = BaseContext.getCurrentId(); // 获取当前用户ID（通过上下文保存）

        // 4. 根据不同的数据库操作类型，为对应属性赋值
        if (operationType == OperationType.INSERT) {
            // 如果是INSERT操作，填充创建时间、创建人、更新时间、更新人
            //这个其实就是反射中，如何获取成员方法的，通过反射获取类对象，然后通过反射获取类对象的方法，最后通过反射调用方法
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);

            // 使用反射调用方法，为字段赋值
            setUpdateTime.invoke(entity, now); // 填充更新时间
            setUpdateUser.invoke(entity, currentId); // 填充更新人
            setCreateTime.invoke(entity, now); // 填充创建时间
            setCreateUser.invoke(entity, currentId); // 填充创建人
        } else if (operationType == OperationType.UPDATE) {
            // 如果是UPDATE操作，仅填充更新时间和更新人
            //这个其实就是反射中，如何获取成员方法的，通过反射获取类对象，然后通过反射获取类对象的方法，最后通过反射调用方法
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            setUpdateTime.invoke(entity, now); // 填充更新时间
            setUpdateUser.invoke(entity, currentId); // 填充更新人
        }
    }
}
