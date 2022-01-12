package com.atguigu.srb.core.controller.api;

import com.atguigu.common.result.R;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.service.BorrowInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "借款管理")
@RestController
@RequestMapping("/admin/core/borrowInfo")
@Slf4j
public class AdminBorrowInfoController {
    @Resource
    private BorrowInfoService borrowInfoService;

    @ApiOperation("借款信息列表")
    @GetMapping("/list")
    public R list() {
        return R.ok().data("list", borrowInfoService.selectList());
    }

    @ApiOperation("获取借款信息")
    @GetMapping("/show/{id}")
    public R show(@ApiParam(value = "借款id", required = true) @PathVariable Long id) {
        return R.ok().data("borrowInfoDetail", borrowInfoService.getBorrowInfoDetail(id));
    }

    @ApiOperation("审批借款信息")
    @PostMapping("/approval")
    public R approval(@RequestBody BorrowInfoApprovalVO borrowInfoApprovalVO) {
        borrowInfoService.approval(borrowInfoApprovalVO);

        return R.ok().message("审批完成");
    }
}
