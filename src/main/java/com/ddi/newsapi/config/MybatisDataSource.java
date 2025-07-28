package com.ddi.newsapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MybatisDataSource {

	private final MybatisConfig mybatisConfig;
	
	/**
	 * <dl>
	 * <dt>DataSourceTransactionManager 생성</dt>
	 * </dl>
	 * 
	 * @return DataSourceTransactionManager
	 */
	@Bean
	public DataSourceTransactionManager transactionManager() {
		
		if(log.isDebugEnabled()){
			log.debug("> transactionManager");
		}
		return new DataSourceTransactionManager(mybatisConfig.dataSource());
	}
}
