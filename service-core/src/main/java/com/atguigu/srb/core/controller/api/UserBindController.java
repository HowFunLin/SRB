package com.atguigu.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.atguigu.common.result.R;
import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.pojo.vo.UserBindVO;
import com.atguigu.srb.core.service.UserBindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 前端控制器
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Api(tags = "会员账号绑定")
@RestController
@RequestMapping("/api/core/userBind")
@Slf4j
public class UserBindController {
    @Resource
    private UserBindService userBindService;

    @ApiOperation("账户绑定提交数据")
    @PostMapping("/auth/bind")
    public R bind(
            @ApiParam(value = "用户绑定信息", required = true) @RequestBody UserBindVO userBindVO,
            HttpServletRequest request
    ) {
        // 从 Header 中获取 token 并校验登录状态和获取 userId
        Long userId = JwtUtils.getUserId(request.getHeader("token"));

        // 根据 userId 绑定用户
        String form = userBindService.commitBindUser(userBindVO, userId);

        return R.ok().data("formStr", form);
    }

    @ApiOperation("账户绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap()); // hfb 请求参数

        // 使用相同算法计算签名与当前收到的签名进行比对
        if (!RequestHelper.isSignEquals(paramMap))
        {
            log.error("异步回调签名验证失败", JSON.toJSONString(paramMap));

            return "fail"; // 非 success 即可
        }

        userBindService.notify(paramMap);

        return "success";
    }
}

