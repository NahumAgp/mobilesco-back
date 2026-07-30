package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import jakarta.servlet.http.Cookie;

class RefreshTokenCookieServiceTest {

    private final RefreshTokenCookieService service =
            new RefreshTokenCookieService("refresh_token", "XSRF-TOKEN", true, "Lax", 30);

    @Test
    void refreshCookieIsHttpOnlySecureAndScoped() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.set(response, "raw-secret");

        List<String> headers = response.getHeaders("Set-Cookie");
        assertEquals(2, headers.size());
        String refresh = headers.stream()
                .filter(value -> value.startsWith("refresh_token="))
                .findFirst()
                .orElseThrow();
        assertTrue(refresh.contains("HttpOnly"));
        assertTrue(refresh.contains("Secure"));
        assertTrue(refresh.contains("SameSite=Lax"));
        assertTrue(refresh.contains("Path=/api/v1/auth"));
        assertTrue(headers.stream()
                .filter(value -> value.startsWith("XSRF-TOKEN="))
                .allMatch(value -> value.contains("Path=/") && !value.contains("HttpOnly")));
    }

    @Test
    void csrfRequiresMatchingCookieAndHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("XSRF-TOKEN", "csrf-value"));
        request.addHeader(RefreshTokenCookieService.CSRF_HEADER, "csrf-value");

        assertDoesNotThrow(() -> service.validateCsrf(request));

        request.removeHeader(RefreshTokenCookieService.CSRF_HEADER);
        request.addHeader(RefreshTokenCookieService.CSRF_HEADER, "different");
        assertThrows(AccessDeniedException.class, () -> service.validateCsrf(request));
    }
}
