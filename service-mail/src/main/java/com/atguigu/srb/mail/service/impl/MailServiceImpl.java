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
    public void sendVerificationCode(String email, String code) {
        SimpleMailMessage simpleMailMessage = getSimpleMailMessage(email);
        simpleMailMessage.setSubject("SRB Verification Code");
        simpleMailMessage.setText("您的验证码为：" + code + "，该验证码 3 分钟内有效，请勿泄露于他人。");

        handleException(simpleMailMessage);
    }

    @Override
    public void sendRechargeNotification(String email, String message) {
        SimpleMailMessage simpleMailMessage = getSimpleMailMessage(email);
        simpleMailMessage.setSubject("SRB Recharge Notification");
        simpleMailMessage.setText("充值成功，本次充值金额为：" + message + "元，预祝投资顺利。");

        handleException(simpleMailMessage);
    }

    /**
     * 通用邮件发送异常处理
     *
     * @param simpleMailMessage 邮件消息对象
     */
    private void handleException(SimpleMailMessage simpleMailMessage) {
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

    /**
     * 获取通用邮件消息对象
     *
     * @param email 邮箱
     * @return 通用邮件消息对象
     */
    private SimpleMailMessage getSimpleMailMessage(String email) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setFrom("SRB_Management<1003941051@qq.com>");
        simpleMailMessage.setTo(email);

        return simpleMailMessage;
    }
}
