package com.atguigu.srb.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 罗海强
 * @version 1.0
 * @date 2021/4/28 9:55
 */
@Slf4j
@NoArgsConstructor //无参构造函数
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO> {

    //ExcelDictDTOListener不受Spring管理，所以不能直接调用DictMapper进行save操作
    private DictMapper dictMapper;

    //数据列表
    private List<ExcelDictDTO> list = new ArrayList<ExcelDictDTO>();

    //每5条记录批量存储一次数据
    private static final int BATCH_COUNT = 5;

    //可以通过有参构造函数注入Mapper
    public ExcelDictDTOListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    @Override
    public void invoke(ExcelDictDTO data, AnalysisContext analysisContext) {
        log.info("解析到一条记录：{}", data);
        //将数据存入数据列表
        list.add(data);
        if(list.size() >= BATCH_COUNT){
            //调用mapper层的save方法
            saveData();
            list.clear();
        }

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //当最后剩余的数据记录数不足BATCH_COUNT时，一次性存储剩余数据
        saveData();
        log.info("所有数据解析完成！");
    }

    private void saveData() {
        log.info("{} 条数据被存储到数据库......", list.size());
        //调用mapper层的批量save方法: save list对象
        dictMapper.insertBatch(list);
        log.info("{} 条数据被存储到数据库成功！", list.size());
    }
}
