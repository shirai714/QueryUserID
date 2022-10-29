package com.linhei.queryuserid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author linhei
 */
@SpringBootApplication
public class QueryUserIdApplication implements WebMvcConfigurer {

    /**
     * 跨域处理
     *
     * @param registry registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://linhei714.club")
                .allowCredentials(true);
    }

    public static void main(String[] args) {
        SpringApplication.run(QueryUserIdApplication.class, args);
    }

}
