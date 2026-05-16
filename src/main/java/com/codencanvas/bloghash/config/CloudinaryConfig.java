package com.codencanvas.bloghash.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.codencanvas.bloghash.config.properties.CloudinaryProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {
 
    private final CloudinaryProperties props;
 
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
            "cloud_name", props.cloudName(),
            "api_key",    props.apiKey(),
            "api_secret", props.apiSecret(),
            "secure",     true   // دايماً https
        ));
    }
}