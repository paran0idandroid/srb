package com.atguigu.srb.sms.receiver;

import com.atguigu.common.util.RandomUtils;
import com.atguigu.srb.base.dto.SmsDTO;
import com.atguigu.srb.rabbitutil.constant.MQConst;
import com.atguigu.srb.sms.service.SmsService;
import com.atguigu.srb.sms.util.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @author 罗海强
 * @version 1.0
 * @date 2021/8/8 21:59
 */

@Component
@Slf4j
public class SmsReceiver {

    @Resource
    private SmsService smsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_SMS_ITEM, durable = "true"),
            exchange = @Exchange(value = MQConst.EXCHANGE_TOPIC_SMS),
            key = {MQConst.ROUTING_SMS_ITEM}
    ))
    public void send(SmsDTO smsDTO){

        log.info("SmsReceiver消息监听......");
        String[] mobile = new String[]{"+86" + smsDTO.getMobile()};
        //String param = smsDTO.getMessage();
        String param = RandomUtils.getFourBitRandom();
        String[] templateParamSet = {param, "1"};
        smsService.send(
                mobile,
                SmsProperties.TEMPLATE_CODE,
                SmsProperties.SIGN_NAME,
                SmsProperties.SDKAPP_ID,
                templateParamSet);
    }
}
