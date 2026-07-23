package com.taskflow.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private static final Key SECRE_KEY= Jwts.SIG.HS256.key().build();
    private static final Long EXPIRATION_TIME= 86400000L;
    public String generateToken(String username,String role){
        return Jwts.builder()
                .claim("role", role)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
                .signWith(SECRE_KEY)
                .compact();
    }

    public String extractUsername(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) SECRE_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public String extractRole(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) SECRE_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public  boolean isTokenValid(String token){

        try{ Jwts.parser()
                .verifyWith((SecretKey) SECRE_KEY)
                .build()
                .parseSignedClaims(token);
        return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
