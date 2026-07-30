package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RefreshTokenCookieService {

    public static final String CSRF_HEADER = "X-CSRF-TOKEN";

    private final SecureRandom secureRandom = new SecureRandom();
    private final String refreshCookieName;
    private final String csrfCookieName;
    private final boolean secure;
    private final String sameSite;
    private final Duration maxAge;

    public RefreshTokenCookieService(
            @Value("${security.refresh-token.cookie-name:refresh_token}") String refreshCookieName,
            @Value("${security.refresh-token.csrf-cookie-name:XSRF-TOKEN}") String csrfCookieName,
            @Value("${security.refresh-token.cookie-secure:true}") boolean secure,
            @Value("${security.refresh-token.cookie-same-site:Lax}") String sameSite,
            @Value("${security.jwt.refresh-ttl-days}") long refreshTtlDays) {
        this.refreshCookieName = refreshCookieName;
        this.csrfCookieName = csrfCookieName;
        this.secure = secure;
        this.sameSite = sameSite;
        this.maxAge = Duration.ofDays(refreshTtlDays);
    }

    public void set(HttpServletResponse response, String refreshToken) {
        add(response, cookie(refreshCookieName, refreshToken, true, "/api/v1/auth", maxAge));
        add(response, cookie(csrfCookieName, randomToken(), false, "/", maxAge));
    }

    public void clear(HttpServletResponse response) {
        add(response, cookie(refreshCookieName, "", true, "/api/v1/auth", Duration.ZERO));
        add(response, cookie(csrfCookieName, "", false, "/", Duration.ZERO));
    }

    public String readRefreshToken(HttpServletRequest request) {
        return readCookie(request, refreshCookieName);
    }

    public void validateCsrf(HttpServletRequest request) {
        String cookieToken = readCookie(request, csrfCookieName);
        String headerToken = request.getHeader(CSRF_HEADER);
        if (cookieToken == null || headerToken == null
                || !MessageDigest.isEqual(
                        cookieToken.getBytes(StandardCharsets.UTF_8),
                        headerToken.getBytes(StandardCharsets.UTF_8))) {
            throw new AccessDeniedException("Token CSRF inválido");
        }
    }

    private ResponseCookie cookie(String name, String value, boolean httpOnly, String path, Duration age) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(age)
                .build();
    }

    private void add(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
