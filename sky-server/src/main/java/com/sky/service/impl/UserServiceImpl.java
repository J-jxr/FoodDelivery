package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * UserServiceImpl 实现类，用于处理与用户相关的业务逻辑。
 * 核心功能：微信登录。
 */
@Slf4j // 开启日志记录功能
@Service // 标记为 Spring 服务层组件
public class UserServiceImpl implements UserService {

    // 微信登录接口地址，用于通过微信授权码换取用户的 openid
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties; // 微信相关配置（如 appid、秘钥等）
    @Autowired
    private UserMapper userMapper; // 数据访问对象，用于操作用户数据表

    /**
     * 微信登录的核心逻辑。
     * 用户通过微信授权码登录，若是新用户则会自动注册，随后返回用户信息。
     *
     * @param userLoginDTO 包含微信授权码的登录数据传输对象
     * @return User 返回登录的用户信息
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 获取微信用户的 openid
        String openid = getOpenid(userLoginDTO.getCode());

        // 如果 openid 为 null，说明登录失败，抛出自定义异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED); // 登录失败提示
        }

        // 根据 openid 查询数据库，判断用户是否已注册
        User user = userMapper.getByOpenid(openid);

        // 如果用户不存在（新用户），需要完成注册
        if (user == null) {
            // 创建一个新用户对象
            user = User.builder()
                    .openid(openid) // 设置 openid
                    .createTime(LocalDateTime.now()) // 设置用户创建时间
                    .build();


            // 保存新用户到本地数据库
            userMapper.insert(user);
        }

        // 返回用户对象（无论是新用户还是老用户）
        return user;
    }

    /**
     * 调用微信接口，根据授权码获取用户的 openid。
     *
     * @param code 微信授权码
     * @return String 返回获取到的 openid
     */
    private String getOpenid(String code) {
        // 准备请求微信接口的参数
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid()); // 小程序的 appid
        map.put("secret", weChatProperties.getSecret()); // 小程序的密钥
        map.put("js_code", code); // 用户通过微信授权后返回的授权码
        map.put("grant_type", "authorization_code"); // 固定值，表示授权类型

        /*
        这一步是整个微信登录逻辑的关键，直接从微信服务器获取用户的 openid，以便后续在应用中识别和处理用户身份。
         */
        // 调用微信接口并接收返回的 JSON 数据
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        // 将返回的 JSON 数据解析为对象
        JSONObject jsonObject = JSON.parseObject(json);

        // 从 JSON 中提取 openid
        String openid = jsonObject.getString("openid");
        return openid; // 返回 openid
    }
}
