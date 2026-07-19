package com.velvet.sakura.service;

import com.velvet.sakura.dto.request.*;
import com.velvet.sakura.dto.response.AccountResponse;
import com.velvet.sakura.dto.response.AuthResponse;
import com.velvet.sakura.entity.Account;
import com.velvet.sakura.entity.DeletionToken;
import com.velvet.sakura.entity.PasswordResetToken;
import com.velvet.sakura.entity.VerificationToken;
import com.velvet.sakura.exception.ResourceNotFoundException;
import com.velvet.sakura.repository.AccountRepository;
import com.velvet.sakura.repository.DeletionTokenRepository;
import com.velvet.sakura.repository.PasswordResetTokenRepository;
import com.velvet.sakura.repository.ReadingRepository;
import com.velvet.sakura.repository.VerificationTokenRepository;
import com.velvet.sakura.security.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final DeletionTokenRepository deletionTokenRepository;
    private final ReadingRepository readingRepository;

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (accountRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("El nombre ya está registrado");
        }
        if (accountRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Account account = Account.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .avatarKey(request.getAvatarKey() != null ? request.getAvatarKey() : "default")
                .build();

        Account saved = accountRepository.save(account);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .accountId(saved.getId())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(saved.getEmail(), saved.getName(), token);

        return toResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository.findByName(request.getName()).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        if (!account.isEnabled()) {
            throw new IllegalStateException("Debes verificar tu cuenta antes de iniciar sesión. Revisa tu correo.");
        }

        String token = jwtService.generateToken(account.getName());
        return new AuthResponse(toResponse(account), token);
    }

    @Override
    public List<AccountResponse> findByName(String name) {
        return accountRepository.findByName(name).stream().map(this::toResponse).toList();
    }

    @Override
    public AccountResponse updateName(Long id, String newName) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        if (!account.getName().equals(newName) && accountRepository.existsByName(newName)) {
            throw new IllegalArgumentException("El nombre ya está registrado");
        }

        account.setName(newName);
        return toResponse(accountRepository.save(account));
    }

    @Override
    public boolean nameExists(String name) {
        return accountRepository.existsByName(name);
    }

    @Override
    public boolean emailExists(String email) {
        return accountRepository.existsByEmail(email.toLowerCase().trim());
    }

    @Override
    public void verifyAccount(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Enlace de verificación no válido"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El enlace de verificación ha caducado");
        }

        Account account = accountRepository.findById(verificationToken.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        account.setEnabled(true);
        accountRepository.save(account);
        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    public void requestPasswordReset(String email) {
        Account account = accountRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("No existe ninguna cuenta con ese email"));

        passwordResetTokenRepository.deleteByAccountId(account.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .accountId(account.getId())
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(account.getEmail(), account.getName(), token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Enlace de recuperación no válido"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El enlace de recuperación ha caducado");
        }

        Account account = accountRepository.findById(resetToken.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        passwordResetTokenRepository.delete(resetToken);
    }

    @Override
    public AccountResponse updateAvatar(Long id, String avatarKey) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
        account.setAvatarKey(avatarKey);
        return toResponse(accountRepository.save(account));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getName(), account.getEmail(), account.getAvatarKey());
    }

    @Override
    public void requestAccountDeletion(Long id, String password) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        deletionTokenRepository.deleteByAccountId(account.getId());

        String token = UUID.randomUUID().toString();
        DeletionToken deletionToken = DeletionToken.builder()
                .token(token)
                .accountId(account.getId())
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();
        deletionTokenRepository.save(deletionToken);

        emailService.sendAccountDeletionEmail(account.getEmail(), account.getName(), token);
    }

    @Override
    public void confirmAccountDeletion(String token) {
        DeletionToken deletionToken = deletionTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Enlace de eliminación no válido"));

        if (deletionToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El enlace de eliminación ha caducado");
        }

        Long accountId = deletionToken.getAccountId();

        readingRepository.deleteAll(readingRepository.findByUserId(accountId));
        verificationTokenRepository.deleteByAccountId(accountId);
        passwordResetTokenRepository.deleteByAccountId(accountId);
        deletionTokenRepository.delete(deletionToken);
        accountRepository.deleteById(accountId);
    }
}