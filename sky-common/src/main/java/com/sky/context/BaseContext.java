package com.sky.context;

/**
 * BaseContext 类用于管理当前线程的上下文数据（如当前用户的ID）。
 * 通过使用 ThreadLocal，确保每个线程都有独立的上下文数据，线程之间的数据互不干扰。
 * 通常用于在请求生命周期中保存用户身份信息，供业务逻辑和安全验证使用。
 */
public class BaseContext {

    /**
     * ThreadLocal 用于存储当前线程的上下文数据（这里是用户ID）。
     * 每个线程都有自己的独立副本，不会与其他线程的数据冲突。
     */
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID到线程上下文中。
     *
     * @param id 要存储的用户ID
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 从线程上下文中获取当前线程存储的用户ID。
     *
     * @return 当前线程的用户ID，如果未设置则返回 null
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 从线程上下文中移除当前线程存储的用户ID。
     * 通常在请求处理结束时调用，避免内存泄漏。
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }
}
