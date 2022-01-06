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

//@CrossOrigin
@RestController
@Api(tags = "积分等级管理")
@RequestMapping("/admin/core/integralGrade")
public class AdminIntegralGradeController {
    @Resource
    private IntegralGradeService integralGradeService;

    @GetMapping("/list")
    @ApiOperation("积分等级列表")
    public R listAll() {
        return R.ok().data("list", integralGradeService.list());
    }

    @DeleteMapping("/remove/{id}")
    @ApiOperation(value = "根据 ID 删除积分等级", notes = "逻辑删除")
    public R removeById(@ApiParam(value = "主键 ID", required = true, example = "1") @PathVariable Long id) {
        return integralGradeService.removeById(id) ? R.ok().message("删除成功") : R.error().message("删除失败");
    }

    @ApiOperation("增加积分等级")
    @PostMapping("/save")
    public R save(@ApiParam(value = "积分等级对象", required = true) @RequestBody IntegralGrade integralGrade) {
        Assert.notNull(integralGrade.getBorrowAmount(), ResponseEnum.BORROW_AMOUNT_NULL_ERROR); // 借款额度为空时抛出自定义异常

        return integralGradeService.save(integralGrade) ? R.ok().message("保存成功") : R.error().message("保存失败");
    }

    @ApiOperation("根据 ID 查询积分等级")
    @GetMapping("/get/{id}")
    public R getById(@ApiParam(value = "主键 ID", required = true, example = "1") @PathVariable Long id) {
        IntegralGrade integralGrade = integralGradeService.getById(id);

        return integralGrade != null ? R.ok().data("record", integralGrade) : R.error().message("不存在指定积分等级");
    }

    @ApiOperation("更新积分等级")
    @PutMapping("/update")
    public R updateById(@ApiParam(value = "积分等级对象", required = true) @RequestBody IntegralGrade integralGrade) {
        return integralGradeService.updateById(integralGrade) ? R.ok().message("更新成功") : R.error().message("更新失败");
    }
}
