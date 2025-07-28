package com.ddi.newsapi.config;


import java.io.IOException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@MapperScan(basePackages= {"com.ddi.newsapi.repository"})
@RequiredArgsConstructor
public class MybatisConfig {

	private final MybatisSetting properties;
	
	@Bean
	DataSource dataSource() {
		BasicDataSource ret = new BasicDataSource();
		try {
			log.info(properties.getUsername());
			log.info(properties.getPassword());
			log.info(properties.getUrl());
			
			ret.setDriverClassName(properties.getDriverClassName());
			ret.setUsername(properties.getUsername());
			ret.setPassword(properties.getPassword());
			ret.setUrl(properties.getUrl());
			ret.setTestWhileIdle(properties.isTestWhileIdle());
			ret.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRunsMillis());
			ret.setValidationQuery(properties.getValidationQuery());
			ret.setMaxTotal(properties.getMaxTotal());
			ret.setMaxIdle(properties.getMaxIdle());
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		return ret;
	}
	
	/**
	 * <dl>
	 * <dt>Mybatis 설정정보 읽어서 SqlSessionFactoryBean 생성</dt>
	 * <dd>DataSource 정보</dd>
	 * <dd>설정파일 (mybatis-config.xml) 경로</dd>
	 * <dd>매퍼파일 경로</dd>
	 * </dl>
	 * 
	 * @param dataSource
	 * @return SqlSessionFactoryBean
	 * @throws IOException
	 */
	@Bean
	public SqlSessionFactoryBean sqlSessionFactoryForMyBatis(DataSource dataSource) throws IOException {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setConfigLocation(properties.getConfigLocation());
		sqlSessionFactoryBean.setMapperLocations(properties.getMapperLocations());

		return sqlSessionFactoryBean;
	}

}
