package com.atguigu.srb.core.controller.admin;


import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.R;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author Riyad
 * @since 2021-12-19
 */
@Api(tags = "积分等级接口")
@CrossOrigin
@RestController
@RequestMapping("/admin/core/integralGrade")
public class AdminIntegralGradeController {
    @Resource
    private IntegralGradeService integralGradeService;

    @ApiOperation("获取积分等级列表")
    @GetMapping("/list")
    public R listAll() {
        return R.ok().message("获取积分等级列表成功").data("list", integralGradeService.list());
    }

    @ApiOperation(value = "根据主键ID删除积分等级", notes = "逻辑删除")
    @DeleteMapping("/remove/{id}")
    public R removeById(@ApiParam(value = "主键ID", example = "1", required = true) @PathVariable Long id) {
        boolean result = integralGradeService.removeById(id);

        if (result)
            return R.ok().message("删除成功");
        else
            return R.error().message("删除失败");
    }

    @ApiOperation("新增积分等级")
    @PostMapping("/save")
    public R save(@ApiParam(value = "积分等级对象", required = true) @RequestBody IntegralGrade integralGrade) {
        Assert.notNull(integralGrade.getBorrowAmount(), ResponseEnum.BORROW_AMOUNT_NULL_ERROR);

        boolean result = integralGradeService.save(integralGrade);

        if (result)
            return R.ok().message("保存成功");
        else
            return R.error().message("保存失败");
    }

    @ApiOperation("根据主键ID获取积分等级")
    @GetMapping("/get/{id}")
    public R getById(@ApiParam(value = "主键ID", example = "1", required = true) @PathVariable Long id) {
        IntegralGrade integralGrade = integralGradeService.getById(id);

        if (integralGrade != null)
            return R.ok().data("record", integralGrade);
        else
            return R.error().message("积分等级获取失败");
    }

    @ApiOperation("更新积分等级")
    @PutMapping("/update")
    public R update(@ApiParam(value = "积分等级对象", required = true) @RequestBody IntegralGrade integralGrade) {
        boolean result = integralGradeService.updateById(integralGrade);

        if (result)
            return R.ok().message("更新成功");
        else
            return R.error().message("更新失败");
    }
}

