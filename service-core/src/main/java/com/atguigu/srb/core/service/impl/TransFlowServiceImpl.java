package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.core.mapper.TransFlowMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.TransFlow;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.service.TransFlowService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 交易流水表 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Service
public class TransFlowServiceImpl extends ServiceImpl<TransFlowMapper, TransFlow> implements TransFlowService {
    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public void saveTransFlow(TransFlowBO transFlowBO) {
        UserInfo userInfo = userInfoMapper.selectOne(new QueryWrapper<UserInfo>().eq("bind_code", transFlowBO.getBindCode()));
        TransFlow transFlow = new TransFlow();

        transFlow.setTransAmount(transFlowBO.getAmount());
        transFlow.setMemo(transFlowBO.getMemo());
        transFlow.setTransTypeName(transFlowBO.getTransTypeEnum().getTransTypeName());
        transFlow.setTransType(transFlowBO.getTransTypeEnum().getTransType());
        transFlow.setTransNo(transFlowBO.getAgentBillNo()); // 流水号

        transFlow.setUserId(userInfo.getId());
        transFlow.setUserName(userInfo.getName());

        baseMapper.insert(transFlow);
    }

    @Override
    public boolean isSavedTransFlow(String agentBillNo) {
        return baseMapper.selectCount(new QueryWrapper<TransFlow>().eq("trans_no", agentBillNo)) > 0;
    }

    @Override
    public List<TransFlow> selectByUserId(Long userId) {
        return baseMapper.selectList(
                new QueryWrapper<TransFlow>().eq("user_id", userId).orderByDesc("id")
        );
    }
}
