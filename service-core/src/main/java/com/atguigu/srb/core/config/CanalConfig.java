package com.atguigu.srb.core.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

/**
 * 读取 Canal 的相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "canal")
public class CanalConfig {
    private String ip;
    private Integer port;
    private String destination;

    @Bean
    public CanalConnector canalConnector() {
        return CanalConnectors.newSingleConnector(new InetSocketAddress(ip, port), destination, "", "");
    }
}
