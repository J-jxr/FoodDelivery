package com.sky.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * JacksonObjectMapper 是一个自定义的 ObjectMapper 类，用于处理 Java 对象与 JSON 之间的转换。
 * 该类继承了 Jackson 的 ObjectMapper 并对其进行了一些自定义配置，主要包括对 LocalDate、LocalDateTime 和 LocalTime 类型的序列化与反序列化操作。
 *
 * Jackson 是一个广泛使用的 JSON 处理库，可以轻松地将 Java 对象与 JSON 格式数据进行相互转换。
 * 在这个类中，特别关注了时间格式的处理，使用了 Java 8 引入的新的时间类（如 LocalDate、LocalDateTime、LocalTime）进行自定义序列化和反序列化。
 */
public class JacksonObjectMapper extends ObjectMapper {

    // 默认日期格式
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    // 默认日期时间格式
    // public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"; // 被注释掉的旧格式
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    // 默认时间格式
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 构造方法，初始化 JacksonObjectMapper 并进行自定义配置。
     * 配置了处理未知属性、反序列化容错、以及时间类的序列化与反序列化。
     */
    public JacksonObjectMapper() {
        super(); // 调用父类的构造方法初始化 ObjectMapper

        // 配置 ObjectMapper，当遇到 JSON 中包含 Java 对象中没有的属性时不抛出异常
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 通过 getDeserializationConfig().withoutFeatures 方法，设置反序列化时忽略未知属性的报错
        this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 创建一个 SimpleModule 用于注册自定义的序列化和反序列化处理器
        SimpleModule simpleModule = new SimpleModule()
                // 为 LocalDateTime 类型注册自定义的反序列化器，指定日期时间格式
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                // 为 LocalDate 类型注册自定义的反序列化器，指定日期格式
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                // 为 LocalTime 类型注册自定义的反序列化器，指定时间格式
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))
                // 为 LocalDateTime 类型注册自定义的序列化器，指定日期时间格式
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                // 为 LocalDate 类型注册自定义的序列化器，指定日期格式
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                // 为 LocalTime 类型注册自定义的序列化器，指定时间格式
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        // 注册自定义的功能模块，将 SimpleModule 注册到 ObjectMapper 中
        this.registerModule(simpleModule);
    }
}
