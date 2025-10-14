package org.iebbuda.mozi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.iebbuda.mozi.domain.account.external.ExternalApiClientConfig;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableCaching
@PropertySource({"classpath:/application.properties"})
@EnableTransactionManagement
@MapperScan(basePackages = {"org.iebbuda.mozi.domain.user.mapper", "org.iebbuda.mozi.domain.policy.mapper", "org.iebbuda.mozi.domain.product.mapper","org.iebbuda.mozi.domain.profile.mapper", "org.iebbuda.mozi.domain.goal.mapper","org.iebbuda.mozi.domain.account.mapper", "org.iebbuda.mozi.domain.scrap.mapper","org.iebbuda.mozi.domain.recommend.mapper"})
@ComponentScan(basePackages = {
        "org.iebbuda.mozi.domain.user.service",
        "org.iebbuda.mozi.domain.policy.service","org.iebbuda.mozi.domain.policy.util", "org.iebbuda.mozi.domain.product.scheduler",
        "org.iebbuda.mozi.domain.product.service","org.iebbuda.mozi.domain.profile.service","org.iebbuda.mozi.domain.goal.service", "org.iebbuda.mozi.domain.account.service", "org.iebbuda.mozi.domain.account.encrypt", "org.iebbuda.mozi.domain.scrap","org.iebbuda.mozi.domain.security.service", "org.iebbuda.mozi.domain.recommend.service"})

@Import(ExternalApiClientConfig.class)

public class RootConfig {
    @Value("${jdbc.driver}") String driver;
    @Value("${jdbc.url}") String url;
    @Value("${jdbc.username}") String username;
    @Value("${jdbc.password}") String password;

    @Value("${mail.smtp.host}")
    private String mailHost;

    @Value("${mail.smtp.port}")
    private int mailPort;

    @Value("${mail.username}")
    private String mailUsername;

    @Value("${mail.password}")
    private String mailPassword;

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public DataSource dataSource(){

        //커넥션 풀의 설정값을 담은 설정 객체
        HikariConfig config = new HikariConfig();

        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        //빠르고 가벼운 connection pool 라이브러리
        HikariDataSource dataSource = new HikariDataSource(config);

        return dataSource;
    }
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception{
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setDataSource(dataSource());

        return (SqlSessionFactory) sqlSessionFactory.getObject();
    }
    @Bean
    public DataSourceTransactionManager transactionManager(){
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource());
        return manager;
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


    // 메일 센더 빈 추가
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");  // 추가
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");   // 추가
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // 추가
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    public CacheManager cacheManager(){
        return new ConcurrentMapCacheManager("deposits", "deposit","savings","saving");
    }
}

