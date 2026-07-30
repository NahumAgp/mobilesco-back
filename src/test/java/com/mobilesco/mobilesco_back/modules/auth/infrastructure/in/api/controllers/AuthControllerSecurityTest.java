package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import com.mobilesco.mobilesco_back.modules.auth.application.usecases.AccesoService;
import com.mobilesco.mobilesco_back.modules.auth.application.usecases.AuthService;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.security.RefreshTokenCookieService;

import jakarta.servlet.http.Cookie;

class AuthControllerSecurityTest {

    @Test
    void productionLoginKeepsRefreshTokenOutOfJson() {
        AuthService authService = mock(AuthService.class);
        when(authService.login("user@example.com", "password"))
                .thenReturn(new AuthService.TokenPair("access", "refresh-secret"));
        AuthController controller = controller(authService, false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthController.TokenResponse body = controller.login(
                new AuthController.LoginRequest("user@example.com", "password"),
                response).getBody();

        assertEquals("access", body.accessToken());
        assertNull(body.refreshToken());
        assertTrue(response.getHeaders("Set-Cookie").stream()
                .anyMatch(cookie -> cookie.startsWith("refresh_token=") && cookie.contains("HttpOnly")));
    }

    @Test
    void cookieRefreshRequiresCsrfAndTakesPrecedenceOverLegacyBody() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = controller(authService, false);
        MockHttpServletRequest missingCsrf = new MockHttpServletRequest();
        missingCsrf.setCookies(new Cookie("refresh_token", "cookie-token"));

        assertThrows(AccessDeniedException.class, () -> controller.refresh(
                new AuthController.RefreshRequest("body-token"),
                missingCsrf,
                new MockHttpServletResponse()));
        verifyNoInteractions(authService);

        MockHttpServletRequest valid = new MockHttpServletRequest();
        valid.setCookies(
                new Cookie("refresh_token", "cookie-token"),
                new Cookie("XSRF-TOKEN", "csrf"));
        valid.addHeader(RefreshTokenCookieService.CSRF_HEADER, "csrf");
        when(authService.refresh("cookie-token"))
                .thenReturn(new AuthService.TokenPair("new-access", "new-refresh"));

        controller.refresh(
                new AuthController.RefreshRequest("body-token"),
                valid,
                new MockHttpServletResponse());

        verify(authService).refresh("cookie-token");
    }

    private AuthController controller(AuthService authService, boolean legacy) {
        return new AuthController(
                authService,
                mock(AccesoService.class),
                new RefreshTokenCookieService("refresh_token", "XSRF-TOKEN", true, "Lax", 30),
                legacy);
    }
}
