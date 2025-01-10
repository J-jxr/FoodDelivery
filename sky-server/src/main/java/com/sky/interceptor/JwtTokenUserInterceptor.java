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
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截到的请求是否是 Controller 的方法
        // 如果 handler 不是一个 HandlerMethod 实例，则表示拦截的可能是静态资源或其他非动态请求
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        // 1. 从请求头中获取令牌（Token）
        // 获取令牌的名称,   从配置文件的 `jwtProperties` 中读取，通常存储在请求头中
        String token = request.getHeader(jwtProperties.getUserTokenName());

        // 2. 校验令牌的合法性
        try {
            // 打印日志记录收到的令牌
            log.info("jwt校验:{}", token);

            // 使用工具类 `JwtUtil` 对令牌进行解析，验证其合法性
            /*
            使用工具类 JwtUtil 验证令牌，传入密钥 jwtProperties.getUserSecretKey() 和令牌。
            如果令牌有效，会返回解析后的 Claims，其中包含了用户的相关信息（如用户ID）。
             */
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

            // 从解析后的 `Claims` 中获取用户ID
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());

            // 将当前登录用户的ID存储到线程上下文中
            // 线程上下文可以在后续业务逻辑中随时获取用户ID，避免频繁解析令牌
            BaseContext.setCurrentId(userId);

            // 打印日志记录当前用户ID
            log.info("当前用户id：{}", userId);

            // 3. 校验通过，放行
            return true;
        } catch (Exception ex) {
            // 如果校验失败，捕获异常并进行处理

            // 4. 校验不通过，设置 HTTP 响应状态码为 401（未授权）
            response.setStatus(401);

            // 返回 false，表示拦截请求，不再继续执行后续逻辑
            return false;
        }
    }

}
