package com.atguigu.srb.mail.receiver;

import com.atguigu.srb.base.dto.SmsDTO;
import com.atguigu.srb.mail.service.MailService;
import com.atguigu.srb.rabbitutil.constant.MQConst;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SmsReceiver {
    @Resource
    private MailService mailService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MQConst.QUEUE_SMS_ITEM, durable = "true"),
                    exchange = @Exchange(name = MQConst.EXCHANGE_TOPIC_SMS),
                    key = MQConst.ROUTING_SMS_ITEM
            )
    )
    public void send(SmsDTO smsDTO) {
        mailService.sendRechargeNotification(smsDTO.getMobile(), smsDTO.getMessage());
    }
}
