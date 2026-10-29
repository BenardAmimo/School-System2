package com.school.money;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(MoneyProperties.class)
public class MoneyConfig {
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(TransactionTemplate.class)
    public TransactionTemplate moneyTransactionTemplate(PlatformTransactionManager tm) {
        return new TransactionTemplate(tm);
    }
}