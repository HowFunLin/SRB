package com.atguigu.srb.core.controller.api;


import com.atguigu.common.result.R;
import com.atguigu.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Api(tags = "标的")
@RestController
@RequestMapping("/api/core/lend")
@Slf4j
public class LendController {
    @Resource
    private LendService lendService;

    @ApiOperation("获取标的列表")
    @GetMapping("/list")
    public R list() {
        return R.ok().data("lendList", lendService.selectList());
    }

    @ApiOperation("获取标的信息")
    @GetMapping("/show/{id}")
    public R show(@ApiParam(value = "标的 ID", required = true) @PathVariable Long id) {
        return R.ok().data("lendDetail", lendService.getLendDetail(id));
    }

    @ApiOperation("计算投资收益")
    @GetMapping("/getInterestCount/{invest}/{yearRate}/{totalMonth}/{returnMethod}")
    public R getInterestCount(
            @ApiParam(value = "投资金额", required = true) @PathVariable("invest") BigDecimal invest,
            @ApiParam(value = "年化收益", required = true) @PathVariable("yearRate") BigDecimal yearRate,
            @ApiParam(value = "期数", required = true) @PathVariable("totalMonth") Integer totalMonth,
            @ApiParam(value = "还款方式", required = true) @PathVariable("returnMethod") Integer returnMethod
    ) {
        return R.ok().data("interestCount", lendService.getInterestCount(invest, yearRate, totalMonth, returnMethod));
    }
}

