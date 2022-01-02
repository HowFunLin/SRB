package com.atguigu.srb.oss.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * OSS 相关属性配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {
    public static String BUCKET_NAME;
    public static String ENDPOINT;

    private String endpoint;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;

    @PostConstruct
    public void init() {
        BUCKET_NAME = bucketName;
        ENDPOINT = endpoint;
    }

    @Bean(destroyMethod = "shutdown")
    public OSS oss() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, secretAccessKey);
    }
}
