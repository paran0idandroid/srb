package com.atguigu.srb.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author 罗海强
 * @version 1.0
 * @date 2021/6/11 15:03
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceGatewayApplication {

    public static void main(String[] args) {

        try {
            SpringApplication.run(ServiceGatewayApplication.class, args);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
}
