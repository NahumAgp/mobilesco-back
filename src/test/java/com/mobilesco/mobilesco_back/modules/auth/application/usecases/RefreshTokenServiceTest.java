package com.mobilesco.mobilesco_back.modules.auth.application.usecases;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.RefreshTokenModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.RefreshTokenRepository;

class RefreshTokenServiceTest {

    @Test
    void rotateRevokesOldTokenAndLinksReplacement() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        RefreshTokenModel old = activeToken();
        when(repository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(old));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RefreshTokenService service = new RefreshTokenService(repository, 30);

        RefreshTokenService.RotationResult result = service.rotate("old-token");

        assertNotNull(old.getRevokedAt());
        assertNotNull(old.getReplacedBy());
        assertNotNull(result.newRefreshToken());
        assertNotEquals("old-token", result.newRefreshToken());
    }

    @Test
    void replayRevokesSuccessorChain() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        RefreshTokenModel replayed = activeToken();
        replayed.setRevokedAt(LocalDateTime.now().minusMinutes(1));
        RefreshTokenModel successor = activeToken();
        RefreshTokenModel latest = activeToken();
        successor.setReplacedBy(latest);
        replayed.setReplacedBy(successor);
        when(repository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(replayed));
        RefreshTokenService service = new RefreshTokenService(repository, 30);

        assertThrows(BadCredentialsException.class, () -> service.rotate("replayed-token"));

        assertNotNull(successor.getRevokedAt());
        assertNotNull(latest.getRevokedAt());
        verify(repository).save(successor);
        verify(repository).save(latest);
    }

    private RefreshTokenModel activeToken() {
        UsuarioModel user = mock(UsuarioModel.class);
        when(user.getId()).thenReturn(42L);
        RefreshTokenModel token = new RefreshTokenModel();
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        return token;
    }
}
