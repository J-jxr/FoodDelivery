package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT令牌校验的拦截器。
 * 主要功能：
 * 1. 验证管理员用户的JWT令牌是否合法。
 * 2. 将解析出的员工ID存储到线程上下文中，以便后续业务逻辑使用。
 * 3. 如果令牌无效，则返回401状态码，拒绝请求进入Controller层。
 */
@Component
@Slf4j // 使用Lombok提供的日志工具，简化日志记录
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties; // 自动注入JWT相关配置，包括令牌名和签名密钥

    /**
     * 拦截请求并验证JWT令牌。
     * 该方法在请求到达Controller之前执行，用于拦截和处理身份认证。
     *
     * @param request  客户端请求对象
     * @param response 服务端响应对象
     * @param handler  请求目标处理器（可能是Controller方法或静态资源）
     * @return true表示校验成功，放行请求；false表示校验失败，阻止请求进入Controller
     * @throws Exception 如果校验过程中发生错误，抛出异常
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前处理的是否是Controller的方法（排除静态资源等请求）
        if (!(handler instanceof HandlerMethod)) {
            // 如果不是Controller的方法，比如静态资源文件，直接放行请求
            return true;
        }
        // 从请求头中获取JWT令牌，键名由配置文件指定
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        try {
            // 打印日志记录获取到的令牌，便于调试
            log.info("JWT校验开始，令牌内容: {}", token);
            // 校验令牌是否有效，并解析其载荷内容
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            // 从解析后的载荷中提取员工ID（假设JWT中存储了EMP_ID字段）
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            // 打印员工ID，便于追踪请求来源
            log.info("当前员工ID: {}", empId);
            // 将员工ID存储到线程上下文中，供后续业务逻辑使用（通过ThreadLocal隔离）
            BaseContext.setCurrentId(empId);
            // 校验通过，放行请求
            return true;
        } catch (Exception ex) {
            // 如果校验失败（如令牌过期或签名错误），记录异常信息
            log.error("JWT校验失败: {}", ex.getMessage());
            // 设置HTTP响应状态为401（未授权），表示认证失败
            response.setStatus(401);
            // 阻止请求继续进入Controller
            return false;
        }
    }
}
