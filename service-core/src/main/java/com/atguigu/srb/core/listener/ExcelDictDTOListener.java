package com.atguigu.srb.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
// @Component 需要用来缓存数据，不能使用 Spring 容器单例进行管理
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO> {
    private static final int BATCH_COUNT = 5;

    private DictMapper dictMapper;

    private List<ExcelDictDTO> list = new ArrayList<>();

    public ExcelDictDTOListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    @Override
    public void invoke(ExcelDictDTO excelDictDTO, AnalysisContext analysisContext) {
        log.info("解析到一条记录：{}", excelDictDTO);

        list.add(excelDictDTO);

        if (list.size() >= BATCH_COUNT) {
            saveList();
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveList(); // 存储 List 中缓存的数据
        log.info("所有数据解析完成！");
    }

    private void saveList() {
        log.info("{} 条数据存储到数据库", list.size());
        dictMapper.insertBatch(list);
        log.info("{} 条数据存储成功！", list.size());
    }
}
