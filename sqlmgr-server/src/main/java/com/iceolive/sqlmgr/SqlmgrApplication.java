package com.iceolive.sqlmgr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;

/**
 * @author wangmianzhe
 */ //无数据库启动
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
public class SqlmgrApplication {


    private static String port;


    @Value("${server.port}")
    public void setPort(String port) {
        SqlmgrApplication.port = port;
    }

    private static String contextPath;

    @Value("${server.servlet.context-path}")
    public void setContextPath(String contextPath) {
        SqlmgrApplication.contextPath = contextPath;

    }

    public static void main(String[] args) {
        SpringApplication.run(SqlmgrApplication.class, args);

    }

    @EventListener({ApplicationReadyEvent.class})
    void applicationReadyEvent() {
        System.out.println("应用已经准备就绪 ... 启动浏览器");
        String url = "http://localhost:" + port + contextPath + "/index.html";
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
