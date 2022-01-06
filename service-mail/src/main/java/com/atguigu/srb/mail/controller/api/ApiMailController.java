package com.atguigu.srb.mail.controller.api;


import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.R;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.common.util.RandomUtils;
import com.atguigu.common.util.RegexValidateUtils;
import com.atguigu.srb.mail.client.CoreUserInfoClient;
import com.atguigu.srb.mail.service.MailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/mail")
@Api(tags = "邮件管理")
//@CrossOrigin
@Slf4j
public class ApiMailController {
    @Resource
    private MailService mailService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private CoreUserInfoClient coreUserInfoClient;

    @ApiOperation("获取验证码")
    @GetMapping("/send/{email}")
    public R send(@ApiParam(value = "邮箱", required = true) @PathVariable String email) {
        // 校验邮箱号是否为空或者格式错误
        Assert.notEmpty(email, ResponseEnum.EMAIL_NULL_ERROR);
        Assert.isTrue(RegexValidateUtils.checkEmail(email), ResponseEnum.EMAIL_ERROR);

        // 远程调用判断邮箱地址是否已经被注册
        // 若远程调用等待时间过长直接触发熔断，service-core 注册业务还会进行校验
        Assert.isTrue(!coreUserInfoClient.checkMobile(email), ResponseEnum.EMAIL_EXIST_ERROR);

        //生成验证码
        String code = RandomUtils.getSixBitRandom();

        //发送短信
//        mailService.send(email, code);

        //将验证码存入redis
        redisTemplate.opsForValue().set("srb:sms:code:" + email, code, 3, TimeUnit.MINUTES);

        return R.ok().message("邮件发送成功");
    }
}