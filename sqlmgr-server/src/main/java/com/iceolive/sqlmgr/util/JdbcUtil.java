package com.iceolive.sqlmgr.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author:wangmianzhe
 **/
public class JdbcUtil {
    /**
     * 创建jdbcTemplate
     * @param driverClassName
     * @param url
     * @param username
     * @param password
     * @return
     */
    public static JdbcTemplate createJdbcTemplate(String driverClassName, String url, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return new JdbcTemplate(dataSource);
    }
}
