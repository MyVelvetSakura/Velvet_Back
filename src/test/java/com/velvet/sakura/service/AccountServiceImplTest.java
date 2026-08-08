package com.velvet.sakura.service;

import com.velvet.sakura.dto.request.CreateAccountRequest;
import com.velvet.sakura.dto.request.LoginRequest;
import com.velvet.sakura.dto.response.AccountResponse;
import com.velvet.sakura.dto.response.AuthResponse;
import com.velvet.sakura.entity.Account;
import com.velvet.sakura.exception.ResourceNotFoundException;
import com.velvet.sakura.repository.*;
import com.velvet.sakura.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private DeletionTokenRepository deletionTokenRepository;
    @Mock private ReadingRepository readingRepository;
    @Mock private UserProgressRepository userProgressRepository;
    @Mock private UserAchievementRepository userAchievementRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;
    @Mock private GeoLocationService geoLocationService;
    @Mock private Collections collections;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .name("Ragnarok1")
                .email("ragnarok1@gmail.com")
                .passwordHash("hashedPassword")
                .enabled(true)
                .avatarKey("default")
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void createAccount_conDatosValidos_creaLaCuentaYEnviaEmailDeVerificacion() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("NuevaUsuaria");
        request.setEmail("Nueva@Gmail.com");
        request.setPassword("password123");

        when(accountRepository.existsByNameIgnoreCase("NuevaUsuaria")).thenReturn(false);
        when(accountRepository.existsByEmail("nueva@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account a = invocation.getArgument(0);
            a.setId(5L);
            return a;
        });

        AccountResponse response = accountService.createAccount(request);

        assertThat(response.name()).isEqualTo("NuevaUsuaria");
        assertThat(response.email()).isEqualTo("nueva@gmail.com");
        verify(verificationTokenRepository).save(any());
        verify(emailService).sendVerificationEmail(eq("nueva@gmail.com"), eq("NuevaUsuaria"), anyString());
    }

    @Test
    void createAccount_conNombreYaExistenteIgnorandoMayusculas_lanzaExcepcion() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("ragnarok1");
        request.setEmail("otro@gmail.com");
        request.setPassword("password123");

        when(accountRepository.existsByNameIgnoreCase("ragnarok1")).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre ya está registrado");

        verify(accountRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    void createAccount_conEmailYaExistente_lanzaExcepcion() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("OtroNombre");
        request.setEmail("ragnarok1@gmail.com");
        request.setPassword("password123");

        when(accountRepository.existsByNameIgnoreCase("OtroNombre")).thenReturn(false);
        when(accountRepository.existsByEmail("ragnarok1@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email ya está registrado");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void login_conCredencialesCorrectas_devuelveTokenYReseteaContadorDeFallos() {
        LoginRequest request = new LoginRequest();
        request.setName("Ragnarok1");
        request.setPassword("password123");

        account.setFailedLoginAttempts(2);

        when(accountRepository.findByNameIgnoreCase("Ragnarok1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("Ragnarok1")).thenReturn("fake-jwt-token");

        AuthResponse response = accountService.login(request, "203.0.113.1");

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(account.getFailedLoginAttempts()).isZero();
        verify(accountRepository).save(account);
        verify(emailService, never()).sendSecurityAlertEmail(any(), any(), any(), any());
    }

    @Test
    void login_conContrasenaIncorrecta_incrementaContadorYLanzaExcepcion() {
        LoginRequest request = new LoginRequest();
        request.setName("Ragnarok1");
        request.setPassword("contraseñaMala");

        account.setFailedLoginAttempts(0);

        when(accountRepository.findByNameIgnoreCase("Ragnarok1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("contraseñaMala", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> accountService.login(request, "203.0.113.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Contraseña incorrecta");

        assertThat(account.getFailedLoginAttempts()).isEqualTo(1);
        verify(accountRepository).save(account);
        verify(emailService, never()).sendSecurityAlertEmail(any(), any(), any(), any());
    }

    @Test
    void login_alTercerFalloConsecutivo_envíaAlertaDeSeguridadYReseteaContador() {
        LoginRequest request = new LoginRequest();
        request.setName("Ragnarok1");
        request.setPassword("contraseñaMala");

        account.setFailedLoginAttempts(2);

        when(accountRepository.findByNameIgnoreCase("Ragnarok1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("contraseñaMala", "hashedPassword")).thenReturn(false);
        when(geoLocationService.resolveLocation("203.0.113.1")).thenReturn("Madrid, España");

        assertThatThrownBy(() -> accountService.login(request, "203.0.113.1"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(account.getFailedLoginAttempts()).isZero();
        verify(emailService).sendSecurityAlertEmail(
                eq("ragnarok1@gmail.com"), eq("Ragnarok1"), eq("203.0.113.1"), eq("Madrid, España")
        );
    }

    @Test
    void login_conCuentaNoVerificada_lanzaIllegalStateException() {
        LoginRequest request = new LoginRequest();
        request.setName("Ragnarok1");
        request.setPassword("password123");

        account.setEnabled(false);

        when(accountRepository.findByNameIgnoreCase("Ragnarok1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> accountService.login(request, "203.0.113.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("verificar tu cuenta");
    }

    @Test
    void login_conUsuarioInexistente_lanzaResourceNotFoundException() {
        LoginRequest request = new LoginRequest();
        request.setName("NoExiste");
        request.setPassword("password123");

        when(accountRepository.findByNameIgnoreCase("NoExiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.login(request, "203.0.113.1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateName_conNombreYaEnUsoPorOtraCuenta_lanzaExcepcion() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.existsByNameIgnoreCase("OtroUsuario")).thenReturn(true);

        assertThatThrownBy(() -> accountService.updateName(1L, "OtroUsuario"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre ya está registrado");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateName_conElMismoNombreActual_noLanzaErrorYGuardaCorrectamente() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountService.updateName(1L, "Ragnarok1");

        assertThat(response.name()).isEqualTo("Ragnarok1");
        verify(accountRepository, never()).existsByNameIgnoreCase(any());
    }

    @Test
void updateAvatar_conCuentaExistente_actualizaYDevuelveLaNuevaClave() {
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
    when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

    AccountResponse response = accountService.updateAvatar(1L, "sakurachibi");

    assertThat(response.avatarKey()).isEqualTo("sakurachibi");
    assertThat(account.getAvatarKey()).isEqualTo("sakurachibi");
}

@Test
void updateAvatar_conCuentaInexistente_lanzaResourceNotFoundException() {
    when(accountRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> accountService.updateAvatar(999L, "sakurachibi"))
            .isInstanceOf(ResourceNotFoundException.class);
}


@Test
void verifyAccount_conTokenValido_activaLaCuentaYBorraElToken() {
    account.setEnabled(false);
    com.velvet.sakura.entity.VerificationToken token = com.velvet.sakura.entity.VerificationToken.builder()
            .id(1L).token("token-abc").accountId(1L)
            .expiryDate(java.time.LocalDateTime.now().plusHours(1))
            .build();

    when(verificationTokenRepository.findByToken("token-abc")).thenReturn(Optional.of(token));
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

    accountService.verifyAccount("token-abc");

    assertThat(account.isEnabled()).isTrue();
    verify(accountRepository).save(account);
    verify(verificationTokenRepository).delete(token);
}

@Test
void verifyAccount_conTokenCaducado_lanzaIllegalStateExceptionYNoActivaLaCuenta() {
    com.velvet.sakura.entity.VerificationToken token = com.velvet.sakura.entity.VerificationToken.builder()
            .id(1L).token("token-caducado").accountId(1L)
            .expiryDate(java.time.LocalDateTime.now().minusMinutes(5))
            .build();

    when(verificationTokenRepository.findByToken("token-caducado")).thenReturn(Optional.of(token));

    assertThatThrownBy(() -> accountService.verifyAccount("token-caducado"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("caducado");

    verify(accountRepository, never()).save(any());
}

@Test
void verifyAccount_conTokenInexistente_lanzaIllegalArgumentException() {
    when(verificationTokenRepository.findByToken("token-falso")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> accountService.verifyAccount("token-falso"))
            .isInstanceOf(IllegalArgumentException.class);
}


@Test
void requestPasswordReset_conEmailExistente_generaCodigoYEnviaElEmail() {
    when(accountRepository.findByEmail("ragnarok1@gmail.com")).thenReturn(Optional.of(account));

    accountService.requestPasswordReset("ragnarok1@gmail.com");

    verify(passwordResetTokenRepository).deleteByAccountId(1L);
    verify(passwordResetTokenRepository).save(any());
    verify(emailService).sendPasswordResetEmail(eq("ragnarok1@gmail.com"), eq("Ragnarok1"), anyString());
}

@Test
void requestPasswordReset_conEmailInexistente_lanzaResourceNotFoundException() {
    when(accountRepository.findByEmail("noexiste@gmail.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> accountService.requestPasswordReset("noexiste@gmail.com"))
            .isInstanceOf(ResourceNotFoundException.class);
}

@Test
void resetPassword_conCodigoValido_actualizaLaContrasena() {
    com.velvet.sakura.entity.PasswordResetToken resetToken = com.velvet.sakura.entity.PasswordResetToken.builder()
            .id(1L).code("483920").accountId(1L)
            .expiryDate(java.time.LocalDateTime.now().plusMinutes(5))
            .build();

    when(accountRepository.findByEmail("ragnarok1@gmail.com")).thenReturn(Optional.of(account));
    when(passwordResetTokenRepository.findByAccountId(1L)).thenReturn(Optional.of(resetToken));
    when(passwordEncoder.encode("nuevaPassword123")).thenReturn("nuevoHash");

    accountService.resetPassword("ragnarok1@gmail.com", "483920", "nuevaPassword123");

    assertThat(account.getPasswordHash()).isEqualTo("nuevoHash");
    verify(passwordResetTokenRepository).delete(resetToken);
}

@Test
void resetPassword_conCodigoIncorrecto_lanzaIllegalArgumentException() {
    com.velvet.sakura.entity.PasswordResetToken resetToken = com.velvet.sakura.entity.PasswordResetToken.builder()
            .id(1L).code("483920").accountId(1L)
            .expiryDate(java.time.LocalDateTime.now().plusMinutes(5))
            .build();

    when(accountRepository.findByEmail("ragnarok1@gmail.com")).thenReturn(Optional.of(account));
    when(passwordResetTokenRepository.findByAccountId(1L)).thenReturn(Optional.of(resetToken));

    assertThatThrownBy(() -> accountService.resetPassword("ragnarok1@gmail.com", "000000", "nuevaPassword123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("incorrecto");
}

@Test
void resetPassword_conCodigoCaducado_lanzaIllegalStateExceptionYLoBorra() {
    com.velvet.sakura.entity.PasswordResetToken resetToken = com.velvet.sakura.entity.PasswordResetToken.builder()
            .id(1L).code("483920").accountId(1L)
            .expiryDate(java.time.LocalDateTime.now().minusMinutes(1))
            .build();

    when(accountRepository.findByEmail("ragnarok1@gmail.com")).thenReturn(Optional.of(account));
    when(passwordResetTokenRepository.findByAccountId(1L)).thenReturn(Optional.of(resetToken));

    assertThatThrownBy(() -> accountService.resetPassword("ragnarok1@gmail.com", "483920", "nuevaPassword123"))
            .isInstanceOf(IllegalStateException.class);

    verify(passwordResetTokenRepository).delete(resetToken);
}


@Test
void requestAccountDeletion_conContrasenaCorrecta_generaTokenYEnviaElEmail() {
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

    accountService.requestAccountDeletion(1L, "password123");

    verify(deletionTokenRepository).deleteByAccountId(1L);
    verify(deletionTokenRepository).save(any());
    verify(emailService).sendAccountDeletionEmail(eq("ragnarok1@gmail.com"), eq("Ragnarok1"), anyString());
}

@Test
void requestAccountDeletion_conContrasenaIncorrecta_lanzaIllegalArgumentException() {
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("contraseñaMala", "hashedPassword")).thenReturn(false);

    assertThatThrownBy(() -> accountService.requestAccountDeletion(1L, "contraseñaMala"))
            .isInstanceOf(IllegalArgumentException.class);

    verify(emailService, never()).sendAccountDeletionEmail(any(), any(), any());
}

@Test
void confirmAccountDeletion_conTokenValido_borraTodoElRastroDelUsuario() {
    com.velvet.sakura.entity.DeletionToken deletionToken = com.velvet.sakura.entity.DeletionToken.builder()
            .id(1L).token("token-delete").accountId(1L)
            .expiryDate(java.time.LocalDateTime.now().plusHours(1))
            .build();

    when(deletionTokenRepository.findByToken("token-delete")).thenReturn(Optional.of(deletionToken));
    when(readingRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

    accountService.confirmAccountDeletion("token-delete");

    verify(readingRepository).deleteAll(Collections.emptyList());
    verify(verificationTokenRepository).deleteByAccountId(1L);
    verify(passwordResetTokenRepository).deleteByAccountId(1L);
    verify(deletionTokenRepository).delete(deletionToken);
    verify(accountRepository).deleteById(1L);
}

@Test
void confirmAccountDeletion_conTokenCaducado_lanzaIllegalStateExceptionYNoBorraNada() {
    com.velvet.sakura.entity.DeletionToken deletionToken = com.velvet.sakura.entity.DeletionToken.builder()
            .id(1L).token("token-caducado").accountId(1L)
            .expiryDate(java.time.LocalDateTime.now().minusMinutes(1))
            .build();

    when(deletionTokenRepository.findByToken("token-caducado")).thenReturn(Optional.of(deletionToken));

    assertThatThrownBy(() -> accountService.confirmAccountDeletion("token-caducado"))
            .isInstanceOf(IllegalStateException.class);

    verify(accountRepository, never()).deleteById(any());
}
}