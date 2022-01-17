package com.atguigu.srb.core.controller.admin;


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

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Api(tags = "标的管理")
@RestController
@RequestMapping("/admin/core/lend")
@Slf4j
public class AdminLendController {
    @Resource
    private LendService lendService;

    @ApiOperation("标的列表")
    @GetMapping("/list")
    public R list() {
        return R.ok().data("list", lendService.selectList());
    }

    @ApiOperation("获取标的信息")
    @GetMapping("/show/{id}")
    public R show(@ApiParam(value = "标的 ID", required = true) @PathVariable Long id) {
        return R.ok().data("lendDetail", lendService.getLendDetail(id));
    }

    @ApiOperation("放款")
    @GetMapping("/makeLoan/{id}")
    public R makeLoan(@ApiParam(value = "标的id", required = true) @PathVariable("id") Long id) {
        lendService.makeLoan(id);

        return R.ok().message("放款成功"); // 放款为同步调用，不使用自动提交表单，直接获取返回结果
    }
}

