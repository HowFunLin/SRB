package com.atguigu.srb.core.controller.api;


import com.atguigu.common.result.R;
import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.service.LendReturnService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 还款记录表 前端控制器
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Api(tags = "还款计划")
@RestController
@RequestMapping("/api/core/lendReturn")
@Slf4j
public class LendReturnController {
    @Resource
    private LendReturnService lendReturnService;

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public R list(@ApiParam(value = "标的id", required = true) @PathVariable Long lendId) {
        return R.ok().data("list", lendReturnService.selectByLendId(lendId));
    }

    @ApiOperation("用户还款")
    @PostMapping("/auth/commitReturn/{lendReturnId}")
    public R commitReturn(
            @ApiParam(value = "还款计划id", required = true) @PathVariable Long lendReturnId,
            HttpServletRequest request
    ) {
        return R.ok().data(
                "formStr",
                lendReturnService.commitReturn(lendReturnId, JwtUtils.getUserId(request.getHeader("token")))
        );
    }

    @ApiOperation("还款异步回调")
    @PostMapping("/notifyUrl")
    public String notifyUrl(HttpServletRequest request) {
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());

        if (RequestHelper.isSignEquals(paramMap)) { //校验签名
            if ("0001".equals(paramMap.get("resultCode"))) { // 业务成功
                lendReturnService.notify(paramMap);

                return "success";
            }
        }

        return "fail";
    }
}

