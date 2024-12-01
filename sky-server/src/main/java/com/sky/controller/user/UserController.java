package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器，用于处理 C 端用户的相关请求。
 * 提供微信登录功能，并返回用户的登录信息（包括 JWT 令牌）。
 */
@RestController // 标记为控制器类，提供 RESTful 风格的接口
@RequestMapping("/user/user") // 配置请求路径的前缀
@Api("C端用户登录接口") // Swagger 注解，描述接口的作用，用于生成接口文档
@Slf4j // 使用 Lombok 提供的日志记录功能
public class UserController {

    @Autowired
    private UserService userService; // 用户服务层，处理微信登录逻辑

    @Autowired
    private JwtProperties jwtProperties; // 用于获取 JWT 配置项



    /**
     * 微信登录接口，处理用户的登录请求。
     *
     * @param userLoginDTO 包含用户微信授权码的传输对象
     * @return Result<UserLoginVO> 包含用户登录信息（用户 ID、OpenID 和 JWT 令牌）的结果对象
     */
    @PostMapping("/login")
    @ApiOperation("微信登录接口") // Swagger 注解，描述该接口的作用
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        // 日志记录，用于调试或监控用户登录流程
        log.info("微信登录授权码：{}", userLoginDTO.getCode());

        // 调用服务层完成微信登录，返回用户信息
        User user = userService.wxLogin(userLoginDTO);

        // 为微信用户生成 JWT 令牌
        Map<String, Object> claims = new HashMap<>(); // 创建一个 Map 存储 JWT 的自定义声明
        claims.put(JwtClaimsConstant.USER_ID, user.getId()); // 将用户 ID 添加到 JWT 声明中
        // 使用工具类生成 JWT，包含签名密钥、有效时间和声明
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        // 构造返回给前端的用户登录信息
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId()) // 设置用户 ID
                .openid(user.getOpenid()) // 设置用户的微信 OpenID
                .token(token) // 设置生成的 JWT 令牌
                .build();

        // 返回成功的结果，包含用户登录信息
        return Result.success(userLoginVO);
    }


}
