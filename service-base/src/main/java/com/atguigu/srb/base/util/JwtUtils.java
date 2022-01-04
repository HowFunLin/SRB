package com.atguigu.srb.base.util;

import com.atguigu.common.exception.BusinessException;
import com.atguigu.common.result.ResponseEnum;
import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * JWT 工具类
 */
public class JwtUtils {
    private static long tokenExpiration = 24 * 60 * 60 * 1000;
    private static String tokenSignKey = "Atguigu123456";

    /**
     * 创建密钥实例
     */
    private static Key getKeyInstance() {
        byte[] bytes = DatatypeConverter.parseBase64Binary(tokenSignKey);

        return new SecretKeySpec(bytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public static String createToken(Long userId, String userName) {
        return Jwts.builder()
                .setSubject("SRB-USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .claim("userId", userId)
                .claim("userName", userName)
                .signWith(SignatureAlgorithm.HS512, getKeyInstance())
                .compressWith(CompressionCodecs.GZIP)
                .compact();
    }

    /**
     * 判断 token 是否有效
     */
    public static boolean checkToken(String token) {
        if (StringUtils.isEmpty(token))
            return false;

        try {
            Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static Long getUserId(String token) {
        return ((Integer) getClaims(token).get("userId")).longValue();
    }

    public static String getUserName(String token) {
        return (String) getClaims(token).get("userName");
    }

    public static void removeToken(String token) {
        // JwtToken 无需删除，客户端扔掉即可。
    }

    /**
     * 校验 token 并返回 Claims
     */
    private static Claims getClaims(String token) {
        if (StringUtils.isEmpty(token))
            // LOGIN_AUTH_ERROR(-211, "未登录"),
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);

        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);

            return claimsJws.getBody();
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }
}

