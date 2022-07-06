package com.atguigu.srb.sms.util;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 罗海强
 * @version 1.0
 * @date 2021/5/11 10:01
 */
@Data
@Component
//@ConfigurationProperties(prefix = "aliyun.sms")
@ConfigurationProperties(prefix = "tencentyun.sms")
public class SmsProperties implements InitializingBean {

//    region-id: cn-hangzhou
//    key-id: LTAI4G5Svnb2TWBMuKnNT6jY
//    key-secret: N7v6R4V3EJ1SGDZlsqtqo8QyVVMmtQ
//    template-code: SMS_96695865
//    sign-name: 谷粒

    private String regionId;
    private String keyId;
    private String keySecret;
    private String templateCode;
    private String signName;
    private String sdkAppId;

    public static String REGION_Id;
    public static String KEY_ID;
    public static String KEY_SECRET;
    public static String TEMPLATE_CODE;
    public static String SIGN_NAME;
    public static String SDKAPP_ID;

    ////当私有成员被赋值后，此方法自动被调用，从而初始化常量
    @Override
    public void afterPropertiesSet() throws Exception {
        REGION_Id = regionId;
        KEY_ID = keyId;
        KEY_SECRET = keySecret;
        TEMPLATE_CODE = templateCode;
        SIGN_NAME = signName;
        SDKAPP_ID = sdkAppId;
    }
}
