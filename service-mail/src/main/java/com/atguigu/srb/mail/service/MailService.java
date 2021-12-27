package com.atguigu.srb.mail.service;

/**
 * 邮件服务
 */
public interface MailService {
    void send(String email, String code);
}