package com.mobilesco.mobilesco_back.auth;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.models.RefreshTokenModel;
import com.mobilesco.mobilesco_back.models.UsuarioModel;
import com.mobilesco.mobilesco_back.repositories.RefreshTokenRepository;
import com.mobilesco.mobilesco_back.security.TokenHash;

@Service
public class RefreshTokenService {

    // Esto es lo que regresa rotate(): el usuario + el nuevo refresh token
    public record RotationResult(Long userId, String newRefreshToken) {}

    private final RefreshTokenRepository refreshRepo;
    private final long refreshTtlDays;

    public RefreshTokenService(RefreshTokenRepository refreshRepo,
                               @Value("${security.jwt.refresh-ttl-days}") long refreshTtlDays) {
        this.refreshRepo = refreshRepo;
        this.refreshTtlDays = refreshTtlDays;
    }

    /**
     * Se usa en LOGIN:
     * - Crea un refresh token nuevo para ese usuario
     * - Guarda su HASH en BD
     * - Devuelve el token "raw" (el real) para mandarlo al cliente
     */
    public String issue(UsuarioModel user) {
        String raw = randomToken();
        String hash = TokenHash.sha256Hex(raw);

        RefreshTokenModel rt = new RefreshTokenModel();
        rt.setUser(user);
        rt.setTokenHash(hash);
        rt.setExpiresAt(LocalDateTime.now().plusDays(refreshTtlDays));

        refreshRepo.save(rt);
        return raw;
    }

    /**
     * Se usa en /refresh:
     * - Recibe el refresh token viejo (raw)
     * - Busca su hash en BD
     * - Verifica que no esté expirado ni revocado
     * - Revoca el viejo
     * - Crea uno nuevo (rotación)
     * - Regresa: usuario + nuevo refresh raw
     */
    public RotationResult rotate(String oldRefreshRaw) {
        String oldHash = TokenHash.sha256Hex(oldRefreshRaw);

        RefreshTokenModel old = refreshRepo.findByTokenHash(oldHash)
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido"));

        LocalDateTime now = LocalDateTime.now();
        if (!old.isActive(now)) {
            throw new BadCredentialsException("Refresh token expirado o revocado");
        }

        // 1) revocar el token viejo
        old.setRevokedAt(now);

        // 2) crear el nuevo token
        String newRaw = randomToken();
        String newHash = TokenHash.sha256Hex(newRaw);

        RefreshTokenModel next = new RefreshTokenModel();
        next.setUser(old.getUser());
        next.setTokenHash(newHash);
        next.setExpiresAt(now.plusDays(refreshTtlDays));

        // Guardamos el nuevo primero para que tenga ID
        refreshRepo.save(next);

        // 3) enlazar viejo -> nuevo (auditoría)
        old.setReplacedBy(next);
        refreshRepo.save(old);

        return new RotationResult(old.getUser().getId(), newRaw);
    }

    /**
     * Se usa en /logout:
     * - Marca el refresh token como revocado
     */
    public void revoke(String refreshRaw) {
        String hash = TokenHash.sha256Hex(refreshRaw);

        refreshRepo.findByTokenHash(hash).ifPresent(rt -> {
            if (rt.getRevokedAt() == null) {
                rt.setRevokedAt(LocalDateTime.now());
                refreshRepo.save(rt);
            }
        });
    }

    // Genera un token aleatorio fuerte (imposible de adivinar)
    private String randomToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}