package com.polycinema.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        // UTF-8 JSON responses
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(new MediaType("application", "json", StandardCharsets.UTF_8));
        supportedMediaTypes.add(new MediaType("application", "*+json", StandardCharsets.UTF_8));
        converter.setSupportedMediaTypes(supportedMediaTypes);

        // Configure ObjectMapper: ISO-8601 dates, UTF-8 locale
        ObjectMapper mapper = converter.getObjectMapper();
        mapper.setLocale(new java.util.Locale("vi", "VN"));
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        converters.add(0, converter);
    }
}
