package com.atguigu.srb.core.controller.api;


import com.atguigu.common.result.R;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 数据字典 前端控制器
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Api(tags = "数据字典")
@RestController
@RequestMapping("/api/core/dict")
@Slf4j
public class DictController {
    @Resource
    private DictService dictService;

    @GetMapping("/findByDictCode/{dictCode}")
    @ApiOperation("根据 dict_code 字段获取其子节点")
    public R findByDictCode(@ApiParam(value = "数据字典二级节点名称", required = true) @PathVariable() String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);

        return R.ok().data("dictList", list);
    }
}

