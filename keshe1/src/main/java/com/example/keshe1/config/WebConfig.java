package com.example.keshe1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @PostConstruct
    public void init() {
        String projectRoot = System.getProperty("user.dir");
        System.out.println("项目根目录：" + projectRoot);

        String uploadPath = projectRoot + "/uploads/";
        System.out.println("上传目录路径：" + uploadPath);

        File uploadDir = new File(uploadPath);
        System.out.println("上传目录是否存在：" + uploadDir.exists());
        System.out.println("上传目录是否可读：" + uploadDir.canRead());
        System.out.println("上传目录是否可写：" + uploadDir.canWrite());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectRoot = System.getProperty("user.dir");
        String uploadPath = "file:" + projectRoot + "/uploads/";

        System.out.println("配置静态资源映射：");
        System.out.println("URL模式：/uploads/**");
        System.out.println("物理路径：" + uploadPath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }
}