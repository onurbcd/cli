package com.onurbcd.eruservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.onurbcd.eruservice.util.BigDecimalFormatter;
import com.onurbcd.eruservice.util.LocalDateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.onurbcd.eruservice.config.EruConstants.LOCAL_DATE_TIME_PATTERN;

@Configuration
public class EruConfig {

    @Bean
    public ObjectMapper eruMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule()
                        .addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN)))
                        .addSerializer(new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE)));
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
