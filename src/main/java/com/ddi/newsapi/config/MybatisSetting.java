package com.ddi.newsapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="mybatis")
@Data
@Slf4j
public class MybatisSetting {
	
	private String driverClassName;
	private String url;
	private String username;
	private String password;
	private boolean testWhileIdle;
	private long timeBetweenEvictionRunsMillis;
	private String validationQuery;
	private int maxTotal;
	private int maxIdle;
	private Resource configLocation;
	private Resource[] mapperLocations;

}
