package com.marvel.config;

import com.framework.common.BaseEnum;
import com.framework.handler.EnumValueTypeHandler;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.Set;

/**
 * Read DB 读数据库
 *
 * @author Jarvis
 * @date 2018/8/24
 */
//@Configuration
//@EnableConfigurationProperties
//@ConfigurationProperties(prefix = "mysql.datasource.read")
//@MapperScan(basePackages = "com.marvel.mapper.read", sqlSessionTemplateRef = "readSqlSessionTemplate")
public class MyBatisReadConfig {

    @Bean(name = "readDataSource")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "readSqlSessionFactory")
    public SqlSessionFactory readSqlSessionFactory(@Qualifier("readDataSource") DataSource dataSource) {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        try {
            factoryBean.setDataSource(dataSource);
            //加载全局mybatis的配置文件
            factoryBean.setConfigLocation(new DefaultResourceLoader()
                    .getResource("classpath:mybatis/mybatis-config.xml"));
            // 扫描mapper配置文件
            Resource[] mapperResources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:/mybatis/mapper/read/*.xml");
            factoryBean.setMapperLocations(mapperResources);
            factoryBean.setTypeAliasesPackage("classpath*:/com/marvel/entity/*");

            ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
            resolverUtil.find(new ResolverUtil.IsA(BaseEnum.class), "com.marvel.entity");
            Set<Class<? extends Class<?>>> handlerSet = resolverUtil.getClasses();
            for (Class<?> clazz : handlerSet) {
                if (BaseEnum.class.isAssignableFrom(clazz) && !BaseEnum.class.equals(clazz)) {
                    factoryBean.getObject()
                            .getConfiguration()
                            .getTypeHandlerRegistry()
                            .register(clazz, EnumValueTypeHandler.class);
                }
            }
            return factoryBean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Bean(name = "readTransactionManager")
    public DataSourceTransactionManager readTransactionManager(@Qualifier("readDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "readSqlSessionTemplate")
    public SqlSessionTemplate readSqlSessionTemplate(@Qualifier("readSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory);
        return template;
    }
}