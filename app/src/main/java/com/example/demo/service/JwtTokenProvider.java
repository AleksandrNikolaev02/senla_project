package com.example.demo.service;

import com.example.demo.annoration.Loggable;
import com.example.demo.model.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret_key}")
    private String secretKey;
    @Value("${jwt.time-to-live-access-token}")
    private Integer timeToLiveToken;
    @Value("${jwt.issuer}")
    private String issuerToken;
    private final JwtDecoder jwtDecoder;

    @Autowired
    public JwtTokenProvider(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Loggable(process = "генерация токена")
    public String generateToken(UserDetails user) {
        JwtClaimsSet claims = createJwtClaimsSet(user);

        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey.getBytes()));
        var params = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);

        return encoder.encode(params).getTokenValue();
    }

    private JwtClaimsSet createJwtClaimsSet(UserDetails user) {
        return JwtClaimsSet.builder()
                .issuer(issuerToken)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(timeToLiveToken))
                .subject(user.getUsername())
                .claim("role", ((User) user).getRoleUser().getName().toString())
                .claim("id", ((User) user).getId())
                .build();
    }

    @Loggable(process = "получение имени пользователя из токена")
    public String getUsernameFromToken(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            return jwt.getSubject();
        } catch (JwtException e) {
            return "";
        }
    }

    @Loggable(process = "получение ID пользователя из токена")
    public Integer getIdFromToken(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            return Integer.parseInt(jwt.getClaimAsString("id"));
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
