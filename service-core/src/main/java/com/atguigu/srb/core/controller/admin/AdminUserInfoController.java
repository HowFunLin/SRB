package com.atguigu.srb.core.controller.admin;


import com.atguigu.common.result.R;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.query.UserInfoQuery;
import com.atguigu.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Api(tags = "会员管理")
@RestController
@RequestMapping("/admin/core/userInfo")
@Slf4j
@CrossOrigin
public class AdminUserInfoController {
    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("获取会员分页列表")
    @GetMapping("/list/{page}/{limit}") // Get 请求无请求体无法使用 @RequestBody
    public R listPage(
            @ApiParam(value = "当前显示页码", required = true) @PathVariable long page,
            @ApiParam(value = "当前页记录数", required = true) @PathVariable long limit,
            @ApiParam(value = "查询对象") UserInfoQuery userInfoQuery
    ) {
        return R.ok().data("pageModel", userInfoService.listPage(new Page<>(page, limit), userInfoQuery));
    }

    @ApiOperation("锁定和解锁会员")
    @PutMapping("/lock/{id}/{status}")
    public R lock(
            @ApiParam(value = "用户 ID", required = true)@PathVariable Long id,
            @ApiParam(value = "锁定状态", required = true) @PathVariable Integer status
    ) {
        userInfoService.lock(id, status);

        return R.ok().message(status.equals(UserInfo.STATUS_NORMAL) ? "解锁成功" : "锁定成功");
    }
}

