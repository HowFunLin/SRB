package com.atguigu.srb.core.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.atguigu.common.exception.BusinessException;
import com.atguigu.common.result.R;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import com.atguigu.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

@Api(tags = "数据字典管理")
@RestController
@RequestMapping("/admin/core/dict")
@Slf4j
@CrossOrigin
public class AdminDictController {
    @Resource
    private DictService dictService;

    @ApiOperation("Excel 文件数据批量导入数据字典")
    @PostMapping("/import")
    public R batchImport(@ApiParam(value = "Excel文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();

            dictService.importData(inputStream);

            return R.ok().message("批量导入成功");
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);
        }
    }

    @ApiOperation("数据字典数据导出为 Excel 文件")
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        try {
            // 使用 Swagger 会导致各种问题，直接用浏览器或者用 PostMan 进行测试
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");

            // 可以防止中文乱码
            String fileName = URLEncoder.encode("数据字典", "UTF-8").replaceAll("\\+", "%20");

            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), ExcelDictDTO.class).sheet("数据字典").doWrite(dictService.listDictData());
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.EXPORT_DATA_ERROR, e);
        }
    }

    @ApiOperation("根据上级 ID 获取对应节点数据字典列表")
    @GetMapping("/listByParentId/{parentId}")
    public R listByParentId(@ApiParam(value = "上级节点id", required = true) @PathVariable Long parentId) {
        return R.ok().data("list", dictService.listByParentId(parentId));
    }
}
