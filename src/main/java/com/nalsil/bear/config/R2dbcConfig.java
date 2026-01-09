package com.nalsil.bear.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

/**
 * R2DBC 설정 클래스
 * 리액티브 데이터베이스 연결 및 초기화 설정
 */
@Configuration
@EnableR2dbcAuditing
public class R2dbcConfig {

    /**
     * 데이터베이스 초기화 설정
     * schema.sql 및 data.sql 스크립트 실행
     * 테스트 환경에서는 실행하지 않음
     *
     * @param connectionFactory R2DBC 연결 팩토리
     * @return ConnectionFactoryInitializer
     */
    @Bean
    @Profile("!test")
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
//        populator.addScript(new ClassPathResource("schema.sql"));
//        populator.addScript(new ClassPathResource("data.sql.old"));

        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
