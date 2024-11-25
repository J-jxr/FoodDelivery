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
 * JWT令牌校验拦截器。
 * 1. 校验请求中的JWT令牌是否合法。
 * 2. 如果合法，将用户ID存入线程上下文，供后续业务使用。
 * 3. 如果令牌无效，返回401，拒绝请求继续进入Controller。
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties; // JWT配置

    /**
     * 在请求到达Controller之前校验JWT令牌。
     *
     * @param request  客户端请求
     * @param response 服务端响应
     * @param handler  请求目标处理器（Controller方法或其他）
     * @return true表示校验通过，继续处理请求；false表示校验失败，阻止请求
     * @throws Exception 校验过程中发生异常
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 排除非Controller请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 获取请求中的JWT令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        try {
            // 校验并解析JWT令牌
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

            // 从JWT中获取用户ID
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());

            // 存储用户ID到线程上下文
            BaseContext.setCurrentId(userId);

            // 校验通过，放行请求
            return true;

        } catch (Exception ex) {
            // 校验失败，返回401状态码
            response.setStatus(401);
            return false;
        }
    }
}
