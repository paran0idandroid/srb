package com.atguigu.srb.sms.controller.api;

import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.R;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.common.util.RandomUtils;
import com.atguigu.common.util.RegexValidateUtils;
import com.atguigu.srb.sms.client.CoreUserInfoClient;
import com.atguigu.srb.sms.service.SmsService;
import com.atguigu.srb.sms.util.SmsProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

/**
 * @author 罗海强
 * @version 1.0
 * @date 2021/5/11 11:46
 */
@RestController
@RequestMapping("/api/sms")
@Api(tags = "短信管理")
//@CrossOrigin //跨域
@Slf4j
public class ApiSmsController {

    @Resource
    private SmsService smsService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private CoreUserInfoClient coreUserInfoClient;

    @ApiOperation("获取验证码")
    @GetMapping("/send/{mobile}")
    public R send(
            @ApiParam(value = "手机号", required = true)
            @PathVariable String[] mobile){


        for(String m : mobile){
            m = "+86" + m;

            //校验手机号吗不能为空
            Assert.notEmpty(m, ResponseEnum.MOBILE_NULL_ERROR);
            //校验手机号码的合法性
            Assert.isTrue(RegexValidateUtils.checkCellphone(m), ResponseEnum.MOBILE_ERROR);
        }

        //判断手机号是否已经注册
        //在sms中远程调用core的方法
        boolean result = coreUserInfoClient.checkMobile(mobile[0]);
        log.info("result = " + result);
        Assert.isTrue(result == false, ResponseEnum.MOBILE_EXIST_ERROR);


//        //校验手机号吗不能为空
//        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
//        //校验手机号码的合法性
//        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);
        String code = RandomUtils.getFourBitRandom();
        String[] templateParamSet = {code, "1"};
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("code", code);
        //smsService.send(mobile, SmsProperties.TEMPLATE_CODE, SmsProperties.SIGN_NAME, SmsProperties.SDKAPP_ID, templateParamSet);

        //将验证码存入redis
        redisTemplate.opsForValue().set("srb:sms:code:" + mobile[0], code, 5, TimeUnit.MINUTES);

        return R.ok().message("短信发送成功");
    }
}
