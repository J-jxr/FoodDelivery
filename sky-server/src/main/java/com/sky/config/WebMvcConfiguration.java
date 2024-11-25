package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * Web层配置类，用于注册和管理Spring MVC相关组件。
 * 包括拦截器、消息转换器、静态资源映射以及接口文档的配置。
 */
@Configuration
@Slf4j // 日志注解，方便打印日志信息进行调试
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    /**
     * 注入自定义的 JWT 拦截器，用于对管理端的请求进行身份认证。
     */
    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     * 用于对管理端和用户端的请求进行过滤，验证 JWT Token。
     *
     * @param registry 拦截器注册器，用于添加拦截器规则。
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        //注册自定义管理端请求拦截器
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**") // 只拦截管理端相关的请求路径
                .excludePathPatterns("/admin/employee/login"); // 登录接口不拦截


        //注册自定义用户端请求拦截器
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/login")
                .excludePathPatterns("/user/shop/status");
    }

    /**
     * 配置 Knife4j（Swagger）接口文档
     * 为管理端的接口生成分组文档。
     *
     * @return Docket对象，用于生成Swagger文档
     */
    @Bean
    public Docket docket1() {
        log.info("准备生成管理端接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档") // 文档标题
                .version("2.0") // 文档版本号
                .description("苍穹外卖项目接口文档") // 简要描述
                .build();

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理端接口") // 分组名称
                .apiInfo(apiInfo) // 文档信息
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.admin")) // 指定扫描的包路径
                .paths(PathSelectors.any()) // 允许扫描所有路径
                .build();
    }

    /**
     * 配置 Knife4j（Swagger）接口文档
     * 为用户端的接口生成分组文档。
     *
     * @return Docket对象，用于生成Swagger文档
     */
    @Bean
    public Docket docket2() {
        log.info("准备生成用户端接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档") // 文档标题
                .version("2.0") // 文档版本号
                .description("苍穹外卖项目接口文档") // 简要描述
                .build();

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端接口") // 分组名称
                .apiInfo(apiInfo) // 文档信息
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.user")) // 指定扫描的包路径
                .paths(PathSelectors.any()) // 允许扫描所有路径
                .build();
    }

    /**
     * 设置静态资源映射
     * 配置 Knife4j 文档和 WebJars 静态资源的访问路径。
     *
     * @param registry 静态资源注册器
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射...");
        registry.addResourceHandler("/doc.html") // Knife4j 文档的访问路径
                .addResourceLocations("classpath:/META-INF/resources/"); // 文档的资源位置
        registry.addResourceHandler("/webjars/**") // WebJars 静态资源路径
                .addResourceLocations("classpath:/META-INF/resources/webjars/"); // 静态资源的位置
    }

    /**
     * 扩展 Spring MVC 的消息转换器
     * 自定义 JSON 数据的序列化与反序列化行为。
     *
     * @param converters Spring MVC 提供的默认消息转换器集合
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");

        // 创建自定义的 JSON 消息转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();

        // 使用自定义的 JacksonObjectMapper 来管理 JSON 的序列化和反序列化规则
        messageConverter.setObjectMapper(new JacksonObjectMapper());

        // 将自定义的消息转换器放在集合的最前面，优先使用
        converters.add(0, messageConverter);
    }
}
