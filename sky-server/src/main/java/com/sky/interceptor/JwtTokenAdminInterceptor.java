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
 * JWT令牌校验的拦截器，用于在请求进入Controller之前对管理员令牌进行验证。
 * 如果令牌有效，将用户信息存储到线程上下文中；否则返回401状态码。
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties; // JWT相关配置，包括令牌名和密钥

    /**
     * JWT令牌校验方法，在请求进入Controller前执行。
     *
     * @param request  客户端请求对象
     * @param response 服务端响应对象
     * @param handler  请求目标处理器（可能是Controller方法或静态资源）
     * @return true表示通过校验，放行请求；false表示校验失败，阻止请求进入Controller
     * @throws Exception 校验过程中可能抛出的异常
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截的是否为Controller的方法
        if (!(handler instanceof HandlerMethod)) {
            // 如果不是Controller的方法（如静态资源），直接放行请求
            return true;
        }

        // 从请求头中获取JWT令牌，令牌的键名由配置文件指定
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        try {
            // 记录获取到的令牌，用于调试或追踪
            log.info("JWT校验开始，令牌内容: {}", token);

            // 校验令牌并解析载荷
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);

            // 从令牌载荷中获取员工ID，约定载荷中必须包含键EMP_ID
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());

            // 打印解析出的员工ID
            log.info("当前员工ID: {}", empId);

            // 使用ThreadLocal将员工ID存储到上下文中，供后续业务逻辑使用
            BaseContext.setCurrentId(empId);

            // 校验通过，放行请求
            return true;
        } catch (Exception ex) {
            // 如果令牌校验失败（如令牌过期或签名错误），记录异常并返回401状态码
            log.error("JWT校验失败: {}", ex.getMessage());
            response.setStatus(401); // 未授权状态码
            return false; // 阻止请求进入Controller
        }
    }
}
