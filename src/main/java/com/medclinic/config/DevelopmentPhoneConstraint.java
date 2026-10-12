package com.medclinic.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("dev & !sqlserver")
@DependsOn("entityManagerFactory")
public class DevelopmentPhoneConstraint implements InitializingBean {
    private final JdbcTemplate jdbc;

    public DevelopmentPhoneConstraint(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void afterPropertiesSet() {
        jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_users_phone ON users(phone)");
    }
}
