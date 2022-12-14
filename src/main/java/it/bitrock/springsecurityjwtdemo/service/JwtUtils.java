package it.bitrock.springsecurityjwtdemo.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Collection;
import java.util.Date;

import static java.util.stream.Collectors.joining;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private static final long EXPIRE = 60 * 60 * 1000; /* 1h */
    private static final String SECRET = "up the irons up the irons up the irons up the irons up the irons";
    private Key secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generate(String username,
                           Collection<? extends GrantedAuthority> authorities) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + EXPIRE);

        Claims claims = Jwts.claims();
        if (authorities != null && !authorities.isEmpty()) {
            claims.put("roles", authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(joining(",")));
        }
        return Jwts.builder()
                .setClaims(claims)
                .setHeaderParam("typ", "JWT")
                .setSubject(username)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    public boolean validate(String token) {
        return getUsername(token) != null && !isExpired(token);
    }

    public String getUsername(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public boolean isExpired(String token) {
        Claims claims = getClaims(token);
        return claims.getExpiration().before(new Date());
    }

    public String getRoles(String token) {
        Claims claims = getClaims(token);
        return (String) claims.get("roles");
    }

    public Claims getClaims(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(secretKey).build()
                    .parseClaimsJws(token);
            return claimsJws.getBody();
        } catch (Exception e) {
            logger.debug("validate token error ", e);
            return null;
        }
    }
}
