package com.app.guesthouse.bookmyroom.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Chooses the database automatically at startup:
 *  1. Tries MySQL with the settings from application.properties.
 *  2. If MySQL is not running or the login is refused, it falls back to the
 *     built-in H2 database so the application still starts without errors.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    private static final String H2_URL = "jdbc:h2:mem:bookmyroom;DB_CLOSE_DELAY=-1";

    @Value("${spring.datasource.url:" + H2_URL + "}")
    private String url;

    @Value("${spring.datasource.username:root}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Bean
    public DataSource dataSource() {
        if (url.startsWith("jdbc:h2:")) {
            return build(url, "sa", "", "org.h2.Driver");
        }
        DriverManager.setLoginTimeout(5);
        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            log.info("Connected to MySQL at {} as '{}'", url, username);
            return build(url, username, password, "com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            log.warn("==================================================================");
            log.warn(" Could not connect to MySQL: {}", ex.getMessage());
            log.warn(" Using the built-in H2 database instead - the app works normally,");
            log.warn(" but data is lost when the app stops.");
            log.warn("==================================================================");
            return build(H2_URL, "sa", "", "org.h2.Driver");
        }
    }

    private static DataSource build(String jdbcUrl, String user, String pwd, String driver) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(user);
        ds.setPassword(pwd);
        ds.setDriverClassName(driver);
        return ds;
    }
}
