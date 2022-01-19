package com.atguigu.srb.mail.service;

/**
 * 邮件服务
 */
public interface MailService {
    /**
     * 发送验证码到邮箱
     *
     * @param email 邮箱地址
     * @param code  验证码
     */
    void sendVerificationCode(String email, String code);

    /**
     * 发送充值金额通知邮件
     *
     * @param email   邮箱地址
     * @param message 充值金额
     */
    void sendRechargeNotification(String email, String message);
}