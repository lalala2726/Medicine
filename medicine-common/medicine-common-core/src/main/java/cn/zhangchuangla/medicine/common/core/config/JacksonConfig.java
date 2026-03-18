package cn.zhangchuangla.medicine.common.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/21
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @Bean
    public JsonMapper jsonMapper() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(dateTimeFormatter)
        );
        javaTimeModule.addDeserializer(
                LocalDateTime.class,
                new LocalDateTimeDeserializer(dateTimeFormatter)
        );
        javaTimeModule.addSerializer(
                LocalDate.class,
                new LocalDateSerializer(dateFormatter)
        );
        javaTimeModule.addDeserializer(
                LocalDate.class,
                new LocalDateDeserializer(dateFormatter)
        );

        SimpleModule numberToStringModule = new SimpleModule();
        numberToStringModule.addSerializer(Long.class, ToStringSerializer.instance);
        numberToStringModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        numberToStringModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        return JsonMapper.builder()
                .findAndAddModules()
                .addModule(javaTimeModule)
                .addModule(numberToStringModule)
                // 日期不转时间戳
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // 忽略未知字段
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 时区
                .defaultTimeZone(TimeZone.getTimeZone("GMT+8"))
                .defaultDateFormat(new SimpleDateFormat(DATE_TIME_PATTERN))
                .build();
    }
}
