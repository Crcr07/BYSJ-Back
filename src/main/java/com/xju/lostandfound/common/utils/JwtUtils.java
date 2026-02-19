package com.xju.lostandfound.common.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 工具类 (适配 JJWT 0.11.5 版本)
 */
public class JwtUtils {

    // 1. 密钥必须足够长 (至少 256 位 / 32 个字符)，否则报错
    // ⚠️ 绝对不要在密钥里放中文
    private static final String SECRET_STRING = "XjuLostAndFoundCampusProjectSecretKey2026Secure";

    // 2. 使用 Keys.hmacShaKeyFor 将字符串转为安全的 Key 对象
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // 过期时间 7 天 (毫秒)
    private static final long EXPIRE = 604800000L;

    /**
     * 生成 Token
     */
    public static String generateToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRE);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(KEY, SignatureAlgorithm.HS256) // 修正：使用 Key 对象签名
                .compact();
    }

    /**
     * 解析 Token 获取用户名
     */
    public static String getClaimsByToken(String token) {
        try {
            return Jwts.parserBuilder() // 修正：使用 parserBuilder (0.11.x 新语法)
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            // Token 过期或无效
            return null;
        }
    }
}