package com.atguigu.srb.base.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Config {
    @Bean
    public Docket adminApiConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("Admin API")
                .apiInfo(adminApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/admin/.*"))) // 路径选择器
                .build();
    }

    private ApiInfo adminApiInfo() {
        return new ApiInfoBuilder()
                .title("尚融宝后台管理系统-API文档")
                .description("本文档描述了尚融宝后台管理系统接口")
                .version("1.0")
                .contact(new Contact("HowFun", "https://www.github.com/howfunlin", "how-fun@foxmail.com"))
                .build();
    }

    @Bean
    public Docket webApiConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("Web API")
                .apiInfo(webApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/api/.*")))
                .build();
    }

    private ApiInfo webApiInfo() {
        return new ApiInfoBuilder()
                .title("尚融宝网站系统-API文档")
                .description("本文档描述了尚融宝网站系统接口")
                .version("1.0")
                .contact(new Contact("HowFun", "https://www.github.com/howfunlin", "how-fun@foxmail.com"))
                .build();
    }
}
