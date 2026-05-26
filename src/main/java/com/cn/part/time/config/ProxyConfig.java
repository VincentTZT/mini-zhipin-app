package com.cn.part.time.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {

    public Map<String, RouteConfig> routes = new HashMap<>();

    public RouteConfig getRouteConfig(String path) {
        return routes.get(path);
    }
}
