package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.BorrowerAttach;
import com.atguigu.srb.core.pojo.vo.BorrowerAttachVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
public interface BorrowerAttachService extends IService<BorrowerAttach> {
    /**
     * 根据借款人 ID 获取借款人上传的附件信息并封装 VO 列表
     *
     * @param borrowerId 借款人 ID
     * @return 借款人上传的附件信息 VO 列表
     */
    List<BorrowerAttachVO> selectBorrowerAttachVOList(Long borrowerId);
}
