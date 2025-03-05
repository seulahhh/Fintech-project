package com.project.fintech.auth.jwt;

import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final String TOKEN_ISSUER = "Fintech_Service";
    SecretKey key = Jwts.SIG.HS256.key().build();

    /**
     * JWT Token basic builder 생성
     *
     * @param email
     * @return JwtBuilder
     */
    public JwtBuilder tokenBuilder(String email) {
        return Jwts.builder().issuedAt(new Date(System.currentTimeMillis())).issuer(TOKEN_ISSUER)
            .audience().add(email).and().subject(email).signWith(key);
    }

    /**
     * Access Token 생성
     *
     * @param email
     * @return Access Token
     */
    public String generateAccessToken(String email) {
        return tokenBuilder(email).expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
            .compact();
    }

    /**
     * Refresh Token 생성
     *
     * @param email
     * @return Refesh Token
     */
    public String generateRefreshToken(String email) {
        return tokenBuilder(email).compact();
    }

    /**
     * token으로부터 expiration 추출
     *
     * @param token
     * @return Date expiration
     */
    public Date getTokenExpiration(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()
            .getExpiration();
    }

    /**
     * JWT Token으로부터 email 추출
     *
     * @param token
     * @return email
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()
            .getSubject();
    }

    /**
     * JWT Token 유효성 검증
     *
     * @param token
     * @return true / false
     */
    public void verifyToken(String token) throws JwtException {
        try {
            Jwts.parser().verifyWith(key).build().parse(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
