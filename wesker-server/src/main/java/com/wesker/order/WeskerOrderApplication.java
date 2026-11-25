package com.wesker.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement//开启注解方式的事务管理
public class WeskerOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeskerOrderApplication.class, args);
    }
}
