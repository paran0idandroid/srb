package com.atguigu.srb.sms.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

import com.atguigu.common.exception.Assert;
import com.atguigu.common.exception.BusinessException;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.sms.service.SmsService;
import com.atguigu.srb.sms.util.SmsProperties;
import com.google.gson.Gson;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;

import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20190711.models.SendStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;

/**
 * @author 罗海强
 * @version 1.0
 * @date 2021/5/11 11:03
 */

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {
    @Override
    public void send(String[] mobile, String templateCode, String signName, String sdkAppId, String[] templateParamSet) {
        try{

            Credential cred = new Credential(SmsProperties.KEY_ID, SmsProperties.KEY_SECRET);

            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");

            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            /* 实例化一个请求对象，根据调用的接口和实际情况，可以进一步设置请求参数
               您可以直接查询 SDK 源码确定接口有哪些属性可以设置
               属性可能是基本类型，也可能引用了另一个数据结构
               推荐使用 IDE 进行开发，可以方便地跳转查阅各个接口和数据结构的文档说明*/
            SmsClient client = new SmsClient(cred, "", clientProfile);

            SendSmsRequest req = new SendSmsRequest();
            req.setPhoneNumberSet(mobile);
            req.setTemplateID(templateCode);
            req.setSign(signName);
            req.setSmsSdkAppid(sdkAppId);
            req.setTemplateParamSet(templateParamSet);


            SendSmsResponse resp = client.SendSms(req);

            //获取响应结果
//            SendStatus[] sendStatusSet = resp.getSendStatusSet();
//            String[] code = new String[sendStatusSet.length];
//            String[] message = new String[sendStatusSet.length];
//            for (int i = 0; i < sendStatusSet.length; i++) {
//                code[i] = sendStatusSet[i].getCode();
//                Assert.equals("OK", code[i], ResponseEnum.TENCENTYUN_SMS_ERROR);
//                message[i] = sendStatusSet[i].getMessage();
//            }      //此部分暂时不管，有问题

            System.out.println(SendSmsResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
            log.error("腾讯云短信发送SDK调用失败：");
            log.error("ErrorCode=" + e.getErrorCode());
            log.error("ErrorMessage=" + e.getRequestId());
            throw new BusinessException(ResponseEnum.TENCENTYUN_SMS_ERROR, e);
        }


//        //创建远程连接客户端对象
//        DefaultProfile profile = DefaultProfile.getProfile(
//                SmsProperties.REGION_Id,
//                SmsProperties.KEY_ID,
//                SmsProperties.KEY_SECRET);
//        IAcsClient client = new DefaultAcsClient(profile);
//
//        //创建远程连接的请求参数
//        CommonRequest request = new CommonRequest();
//        request.setSysMethod(MethodType.POST);
//        request.setSysDomain("dysmsapi.aliyuncs.com");
//        request.setSysVersion("2017-05-25");
//        request.setSysAction("SendSms");
//        request.putQueryParameter("RegionId", SmsProperties.REGION_Id);
//        request.putQueryParameter("PhoneNumbers", mobile);
//        request.putQueryParameter("SignName", SmsProperties.SIGN_NAME);
//        request.putQueryParameter("TemplateCode", templateCode);
//
//        Gson gson = new Gson();
//        String json = gson.toJson(param);
//        request.putQueryParameter("TemplateParam", json);
//
//        try {
//            //使用客户端对象携带请求对象发送请求并得到响应结果
//            CommonResponse response = client.getCommonResponse(request);
//            System.out.println("response.getData():" + response.getData());
//
//        } catch (ServerException e) {
//            log.error("阿里云短信发送SDK调用失败：");
//            log.error("ErrorCode=" + e.getErrCode());
//            log.error("ErrorMessage=" + e.getErrMsg());
//            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR, e);
//        } catch (ClientException e) {
//            log.error("阿里云短信发送SDK调用失败：");
//            log.error("ErrorCode=" + e.getErrCode());
//            log.error("ErrorMessage=" + e.getErrMsg());
//            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR, e);
//        }
    }
}
