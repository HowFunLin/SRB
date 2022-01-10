package com.atguigu.srb.core.controller.admin;


import com.atguigu.common.result.R;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.BorrowerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 借款人 前端控制器
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Api(tags = "借款人管理")
@RestController
@RequestMapping("/admin/core/borrower")
@Slf4j
public class AdminBorrowerController {
    @Resource
    private BorrowerService borrowerService;

    @ApiOperation("获取借款人分页列表")
    @GetMapping("/list/{page}/{limit}")
    public R listPage(
            @ApiParam(value = "当前页码", required = true) @PathVariable Long page,
            @ApiParam(value = "每页记录数", required = true) @PathVariable Long limit,
            @ApiParam(value = "查询关键字") @RequestParam String keyword
    ) {
        // 在目前的 Swagger 版本中（2.9.2）不能省略， Swagger 默认会将没有注解的参数解析为 Body 中的传递的数据
        return R.ok().data("pageModel", borrowerService.listPage(new Page<>(page, limit), keyword));
    }

    @ApiOperation("获取借款人信息")
    @GetMapping("/show/{id}")
    public R show(@ApiParam(value = "借款人id", required = true) @PathVariable Long id) {
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(id);
        return R.ok().data("borrowerDetailVO", borrowerDetailVO);
    }
}

