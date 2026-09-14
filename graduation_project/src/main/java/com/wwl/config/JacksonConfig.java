package com.wwl.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_TIME_FORMAT_NO_SECOND = "yyyy-MM-dd HH:mm";
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter FULL_FORMAT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private static final DateTimeFormatter SHORT_FORMAT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_NO_SECOND);

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 搴忓垪鍖栫粺涓?鏍煎紡
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(FULL_FORMAT));
        // 鑷畾涔夊鏍煎紡鍙嶅簭鍒楀寲
        javaTimeModule.addDeserializer(LocalDateTime.class, new MultiFormatLocalDateTimeDeserializer());

        objectMapper.registerModule(javaTimeModule);
        // 绂佹杈撳嚭鏃堕棿鎴?        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 蹇界暐鍓嶇浼犵殑鏈煡澶氫綑瀛楁
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return objectMapper;
    }

    /**
     * 鏀寔锛氭椂闂存埑銆亂yyy-MM-dd HH:mm:ss銆亂yyy-MM-dd HH:mm銆佸甫T鏍煎紡
     */
    public static class MultiFormatLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // 13浣嶆绉掓椂闂存埑
            if (p.isExpectedNumberIntToken()) {
                long milli = p.getLongValue();
                return Instant.ofEpochMilli(milli).atZone(ZONE_ID).toLocalDateTime();
            }
            String text = p.getText().trim();
            if (text.isBlank()) {
                return null;
            }
            // T缁熶竴鏇挎崲绌烘牸
            String standard = text.replace("T", " ");
            try {
                return LocalDateTime.parse(standard, FULL_FORMAT);
            } catch (DateTimeParseException e1) {
                try {
                    LocalDateTime time = LocalDateTime.parse(standard, SHORT_FORMAT);
                    return time.withSecond(0).withNano(0);
                } catch (DateTimeParseException e2) {
                    throw new IOException("鏃ユ湡鏍煎紡閿欒锛屾敮鎸佹椂闂存埑銆亂yyy-MM-dd HH:mm:ss銆亂yyy-MM-dd HH:mm");
                }
            }
        }
    }
}
