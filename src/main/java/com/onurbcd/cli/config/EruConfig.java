package com.onurbcd.cli.config;

import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import com.onurbcd.cli.formatter.BigDecimalFormatter;
import com.onurbcd.cli.formatter.LocalDateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.onurbcd.cli.util.Constant.LOCAL_DATE_TIME_PATTERN;

@Configuration
public class EruConfig {

    @Bean
    public ObjectMapper eruMapper() {
        var javaTimeModule = new SimpleModule()
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(
                        DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN)))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));

        return JsonMapper.builder()
                .addModule(javaTimeModule)
                .build();
    }

    @Bean
    public LocalDateTimeFormatter localDateTimeFormatter() {
        return new LocalDateTimeFormatter(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN));
    }

    @Bean
    public BigDecimalFormatter bigDecimalFormatter() {
        return new BigDecimalFormatter(NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")));
    }
}
