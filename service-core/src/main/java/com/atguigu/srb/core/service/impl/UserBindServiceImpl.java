package com.atguigu.srb.core.service.impl;

import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.UserBindEnum;
import com.atguigu.srb.core.hfb.FormHelper;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.UserBindMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.UserBind;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.vo.UserBindVO;
import com.atguigu.srb.core.service.UserBindService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {
    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public String commitBindUser(UserBindVO userBindVO, Long userId) {
        // 不允许不同 user_id，存在相同身份证
        UserBind userBind = baseMapper.selectOne(new QueryWrapper<UserBind>()
                .eq("id_card", userBindVO.getIdCard())
                .ne("user_id", userId));
        Assert.isNull(userBind, ResponseEnum.USER_BIND_ID_CARD_EXIST_ERROR);

        // 校验当前账户是否重复绑定
        userBind = baseMapper.selectOne(new QueryWrapper<UserBind>().eq("user_id", userId));

        // 用户不存在，创建用户绑定记录
        if (userBind == null) {
            userBind = new UserBind();

            BeanUtils.copyProperties(userBindVO, userBind);
            userBind.setUserId(userId);
            userBind.setStatus(UserBindEnum.NO_BIND.getStatus());

            baseMapper.insert(userBind);
        } else {
            // 相同 user_id， 不同身份证，可重新绑定
            BeanUtils.copyProperties(userBindVO, userBind);

            baseMapper.updateById(userBind);
        }

        // 生成动态表单字符串
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentUserId", userId);
        paramMap.put("idCard", userBindVO.getIdCard());
        paramMap.put("personalName", userBindVO.getName());
        paramMap.put("bankType", userBindVO.getBankType());
        paramMap.put("bankNo", userBindVO.getBankNo());
        paramMap.put("mobile", userBindVO.getMobile());
        paramMap.put("returnUrl", HfbConst.USER_BIND_RETURN_URL);
        paramMap.put("notifyUrl", HfbConst.USER_BIND_NOTIFY_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign", RequestHelper.getSign(paramMap));

        return FormHelper.buildForm(HfbConst.USER_BIND_URL, paramMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(Map<String, Object> paramMap) {
        String userId = (String) paramMap.get("agentUserId");
        String bindCode = (String) paramMap.get("bindCode");

        // 更新表使得与远程 hfb 保持一致

        // 更新 user_bind 表
        UserBind userBind = baseMapper.selectOne(new QueryWrapper<UserBind>().eq("user_id", userId));

        userBind.setBindCode(bindCode);
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus());

        baseMapper.updateById(userBind);

        // 更新 user_info 表
        UserInfo userInfo = userInfoMapper.selectById(userId);

        userInfo.setBindCode(bindCode);
        userInfo.setName(userBind.getName());
        userInfo.setIdCard(userBind.getIdCard());
        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus());

        userInfoMapper.updateById(userInfo);
    }

    @Override
    public String getBindCodeByUserId(Long userId) {
        return baseMapper.selectOne(new QueryWrapper<UserBind>().eq("user_id", userId)).getBindCode();
    }
}
