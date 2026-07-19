package com.velvet.sakura.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.velvet.sakura.dto.request.CreateAccountRequest;
import com.velvet.sakura.dto.request.ForgotPasswordRequest;
import com.velvet.sakura.dto.request.ResetPasswordRequest;
import com.velvet.sakura.dto.request.UpdateAvatarRequest;
import com.velvet.sakura.dto.request.LoginRequest;
import com.velvet.sakura.dto.request.RequestDeletionRequest;
import com.velvet.sakura.dto.response.AccountResponse;
import com.velvet.sakura.dto.response.AuthResponse;
import com.velvet.sakura.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse register(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return accountService.login(request);
    }

    @GetMapping(params = "name")
    public List<AccountResponse> getByName(@RequestParam String name) {
        return accountService.findByName(name);
    }

    @PatchMapping("/{id}")
    public AccountResponse updateName(@PathVariable Long id,
            @RequestBody com.velvet.sakura.dto.request.UpdateNameRequest request) {
        return accountService.updateName(id, request.getName());
    }

    @GetMapping("/verify")
    public String verify(@RequestParam String token) {
        accountService.verifyAccount(token);
        return "Cuenta verificada correctamente";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        accountService.requestPasswordReset(request.getEmail());
        return "Si el email existe, se ha enviado un enlace de recuperación";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        accountService.resetPassword(request.getToken(), request.getNewPassword());
        return "Contraseña actualizada correctamente";
    }

    @PatchMapping("/{id}/avatar")
    public AccountResponse updateAvatar(@PathVariable Long id, @Valid @RequestBody UpdateAvatarRequest request) {
        return accountService.updateAvatar(id, request.getAvatarKey());
    }

    @PostMapping("/{id}/request-deletion")
    public String requestDeletion(@PathVariable Long id, @Valid @RequestBody RequestDeletionRequest request) {
        accountService.requestAccountDeletion(id, request.getPassword());
        return "Te hemos enviado un correo para confirmar la eliminación de tu cuenta";
    }

    @GetMapping("/confirm-deletion")
    public String confirmDeletion(@RequestParam String token) {
        accountService.confirmAccountDeletion(token);
        return "Cuenta eliminada correctamente";
    }
}