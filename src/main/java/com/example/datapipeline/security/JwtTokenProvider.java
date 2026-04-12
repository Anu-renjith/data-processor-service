package com.example.datapipeline.security;


import com.example.datapipeline.exception.DataProcessingException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    @Value("${app.jwt.expiration-milliseconds}")
    private long jwtExpirtaionDate;

    //generate JWT token
    public String generateToken(Authentication authentication){
        String username= authentication.getName();
        Date currentDate=new Date();
        Date expireDate=new Date(currentDate.getTime()+jwtExpirtaionDate);
        String token= Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key())
                .compact(); //compact will clumb all this and create token
        return token;
    }

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    //retrieve the subject username from token
    //get username from JWT token
    public String getUserName(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //validate jwt token

    public boolean validateToken(String token){
        try{

            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parse(token);
            return true;
        }catch (MalformedJwtException malformedJwtException) {
            throw new DataProcessingException(HttpStatus.BAD_REQUEST, "invalid JWT token");
        }
        catch(ExpiredJwtException expiredJwtException){
            throw new DataProcessingException(HttpStatus.BAD_REQUEST, "Expired Jwt token");
        }catch(UnsupportedJwtException unsupportedJwtException){
            throw new DataProcessingException(HttpStatus.BAD_REQUEST, "unsupported jwt token");

        }catch(IllegalArgumentException illegalArgumentException){
            throw new DataProcessingException(HttpStatus.BAD_REQUEST,"jwt claims string is null or empty");
        }
    }

}
