package com.atguigu.srb.mail.service.impl;

import com.atguigu.common.exception.BusinessException;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.mail.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class MailServiceImpl implements MailService {
    @Resource
    private JavaMailSender javaMailSender;

    @Override
    public void send(String email, String code) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setFrom("SRB_Registry<1003941051@qq.com>");
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("SRB Verification Code");
        simpleMailMessage.setText("您的验证码为：" + code + "，该验证码 3 分钟内有效，请勿泄露于他人。");

        try {
            javaMailSender.send(simpleMailMessage);
        } catch (MailAuthenticationException e) {
            log.error("邮箱认证失败！");
            throw new BusinessException(ResponseEnum.QQ_MAIL_ERROR, e);
        } catch (MailParseException e) {
            log.error("邮件分析失败！");
            throw new BusinessException(ResponseEnum.QQ_MAIL_ERROR, e);
        } catch (MailSendException e) {
            log.error("邮件发送失败！");
            throw new BusinessException(ResponseEnum.QQ_MAIL_ERROR, e);
        }
    }
}
