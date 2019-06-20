package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials() {
        String json = System.getenv("VCAP_SERVICES");
        return new DatabaseServiceCredentials(json);
    }

    @Bean("albumsDataSource")
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));

        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);

        return hikariDataSource;
    }

    @Bean("moviesDataSource")
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));

        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);

        return hikariDataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.MYSQL);
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setGenerateDdl(true);
        return adapter;
    }

    @Bean(name = "albumsFactory")
    public LocalContainerEntityManagerFactoryBean albumsEntityManagerFactory(@Qualifier("albumsDataSource") DataSource source, HibernateJpaVendorAdapter adapter) {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(source);
        bean.setJpaVendorAdapter(adapter);
        bean.setPackagesToScan(Application.class.getPackage().getName());
        bean.setPersistenceUnitName("albums");
        return bean;
    }

    @Bean(name = "moviesFactory")
    public LocalContainerEntityManagerFactoryBean moviesEntityManagerFactory(@Qualifier("moviesDataSource") DataSource source, HibernateJpaVendorAdapter adapter) {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(source);
        bean.setJpaVendorAdapter(adapter);
        bean.setPackagesToScan(Application.class.getPackage().getName());
        bean.setPersistenceUnitName("movies");
        return bean;
    }

    @Bean(name = "albumsTransactionManager")
    public PlatformTransactionManager albumsTransactionManager(@Qualifier("albumsFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    @Bean(name = "moviesTransactionManager")
    public PlatformTransactionManager moviesTransactionManager(@Qualifier("moviesFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }
}
