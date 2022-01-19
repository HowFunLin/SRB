package com.atguigu.srb.core.controller.api;


import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.R;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.core.pojo.vo.LoginVO;
import com.atguigu.srb.core.pojo.vo.RegisterVO;
import com.atguigu.srb.core.pojo.vo.UserInfoVO;
import com.atguigu.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author Riyad
 * @since 2021-12-27
 */
@Api(tags = "会员接口")
@RestController
@RequestMapping("/api/core/userInfo")
@Slf4j
//@CrossOrigin
public class UserInfoController {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("会员注册")
    @PostMapping("/register")
    public R register(@RequestBody RegisterVO registerVO) {
        String inputCode = registerVO.getCode(), email = registerVO.getMobile(), password = registerVO.getPassword();
        String code = redisTemplate.opsForValue().get("srb:sms:code:" + email);

        Assert.notEmpty(inputCode, ResponseEnum.CODE_NULL_ERROR);
        Assert.notEmpty(email, ResponseEnum.EMAIL_NULL_ERROR);
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);

        Assert.equals(inputCode, code, ResponseEnum.CODE_ERROR); // 验证码校验

        userInfoService.register(registerVO); // 用户注册

        return R.ok().message("注册成功");
    }

    @ApiOperation("会员登录")
    @PostMapping("/login")
    public R login(@RequestBody LoginVO loginVO, HttpServletRequest request) {
        String email = loginVO.getMobile(), password = loginVO.getPassword();

        Assert.notEmpty(email, ResponseEnum.EMAIL_NULL_ERROR);
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);

        String ip = request.getRemoteAddr();

        UserInfoVO userInfoVO = userInfoService.login(loginVO, ip);

        return R.ok().data("userInfo", userInfoVO);
    }

    @ApiOperation("校验令牌")
    @GetMapping("/checkToken")
    public R checkToken(HttpServletRequest request) {
        return JwtUtils.checkToken(request.getHeader("token")) ? R.ok() : R.setResult(ResponseEnum.LOGIN_AUTH_ERROR);
    }

    @ApiOperation("校验邮箱地址是否注册")
    @GetMapping("/checkMobile/{mobile}")
    public boolean checkMobile(@PathVariable String mobile) {
        return userInfoService.checkMobile(mobile);
    }

    @ApiOperation("获取个人空间用户信息")
    @GetMapping("/auth/getIndexUserInfo")
    public R getIndexUserInfo(HttpServletRequest request) {
        return R.ok().data("userIndexVO", userInfoService.getIndexUserInfo(JwtUtils.getUserId(request.getHeader("token"))));
    }
}

