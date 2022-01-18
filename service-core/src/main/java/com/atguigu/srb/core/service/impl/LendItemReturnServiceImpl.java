package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.core.mapper.LendItemMapper;
import com.atguigu.srb.core.mapper.LendItemReturnMapper;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.mapper.LendReturnMapper;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.entity.LendItemReturn;
import com.atguigu.srb.core.service.LendItemReturnService;
import com.atguigu.srb.core.service.UserBindService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借回款记录表 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Service
public class LendItemReturnServiceImpl extends ServiceImpl<LendItemReturnMapper, LendItemReturn>
        implements LendItemReturnService {
    @Resource
    private LendItemMapper lendItemMapper;

    @Resource
    private UserBindService userBindService;

    @Resource
    private LendReturnMapper lendReturnMapper;

    @Resource
    private LendMapper lendMapper;

    @Override
    public List<LendItemReturn> selectByLendId(Long lendId, Long userId) {
        return baseMapper.selectList(new QueryWrapper<LendItemReturn>()
                .eq("lend_id", lendId)
                .eq("invest_user_id", userId)
                .orderByAsc("current_period")
        );
    }

    @Override
    public List<Map<String, Object>> addReturnDetail(Long lendReturnId) {
        List<LendItemReturn> lendItemReturnList = selectLendItemReturnList(lendReturnId);
        List<Map<String, Object>> lendItemReturnDetailList = new ArrayList<>(lendItemReturnList.size());

        Lend lend = lendMapper.selectById(lendReturnMapper.selectById(lendReturnId).getLendId());

        for (LendItemReturn lendItemReturn : lendItemReturnList) {
            LendItem lendItem = lendItemMapper.selectById(lendItemReturn.getLendItemId());

            Map<String, Object> map = new HashMap<>();

            //项目编号
            map.put("agentProjectCode", lend.getLendNo());
            //出借编号
            map.put("voteBillNo", lendItem.getLendItemNo());
            //收款人（出借人）
            map.put("toBindCode", userBindService.getBindCodeByUserId(lendItem.getInvestUserId()));
            //还款金额
            map.put("transitAmt", lendItemReturn.getTotal());
            //还款本金
            map.put("baseAmt", lendItemReturn.getPrincipal());
            //还款利息
            map.put("benifitAmt", lendItemReturn.getInterest());
            //商户手续费
            map.put("feeAmt", BigDecimal.ZERO);

            lendItemReturnDetailList.add(map);
        }

        return lendItemReturnDetailList;
    }

    @Override
    public List<LendItemReturn> selectLendItemReturnList(Long lendReturnId) {
        return baseMapper.selectList(new QueryWrapper<LendItemReturn>().eq("lend_return_id", lendReturnId));
    }
}
