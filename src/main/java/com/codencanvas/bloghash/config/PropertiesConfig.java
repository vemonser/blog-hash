package com.codencanvas.bloghash.config;

import com.codencanvas.bloghash.config.properties.AppSecurityProperties;
import com.codencanvas.bloghash.config.properties.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
 


@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    AppSecurityProperties.class
})
public class PropertiesConfig {}
