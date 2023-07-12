package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.cors.url")
public class CustomProperties {

    /**
     * CorsUrl
     */
    private String corsUrl = "http://localhost:4200/";

    // getter & setter
}
