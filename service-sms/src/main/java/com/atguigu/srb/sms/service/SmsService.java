package com.atguigu.srb.sms.service;

import java.util.Map;

/**
 * @author 罗海强
 * @version 1.0
 * @date 2021/5/11 11:02
 */
public interface SmsService {
    void send(String[] mobile, String templateCode, String signName, String sdkAppId, String[] templateParamSet);
}
