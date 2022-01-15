package com.atguigu.srb.core.controller.api;


import com.atguigu.common.result.R;
import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.service.UserAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 前端控制器
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Api(tags = "会员账户")
@RestController
@RequestMapping("/api/core/userAccount")
@Slf4j
public class UserAccountController {
    @Resource
    private UserAccountService userAccountService;

    @ApiOperation("充值")
    @PostMapping("/auth/commitCharge/{chargeAmt}")
    public R commitCharge(@ApiParam(value = "充值金额", required = true) @PathVariable BigDecimal chargeAmt, HttpServletRequest request) {
        return R.ok().data("formStr", userAccountService.commitCharge(chargeAmt, JwtUtils.getUserId(request.getHeader("token"))));
    }

    @ApiOperation(value = "用户充值异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, Object> map = RequestHelper.switchMap(request.getParameterMap());

        // 验证签名
        if (RequestHelper.isSignEquals(map)) {
            if ("0001".equals(map.get("resultCode"))) // 判断业务是否成功
                userAccountService.notify(map);// 同步账户数据

            return "success"; // 签名验证成功，不管业务是否成功，均返回 success 防止反复重试
        }

        return "fail";
    }

    @ApiOperation("查询账户余额")
    @GetMapping("/auth/getAccount")
    public R getAccount(HttpServletRequest request) {
        return R.ok().data("account", userAccountService.getAccount(JwtUtils.getUserId(request.getHeader("token"))));
    }
}

