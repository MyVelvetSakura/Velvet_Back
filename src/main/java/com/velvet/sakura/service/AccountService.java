package com.velvet.sakura.service;

import com.velvet.sakura.dto.request.*;
import com.velvet.sakura.dto.response.AccountResponse;
import com.velvet.sakura.dto.response.AuthResponse;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest request);

    AuthResponse login(LoginRequest request);

    List<AccountResponse> findByName(String name);

    AccountResponse updateName(Long id, String newName);

    boolean nameExists(String name);

    boolean emailExists(String email);

    void verifyAccount(String token);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    AccountResponse updateAvatar(Long id, String avatarKey);

    void requestAccountDeletion(Long id, String password);

    void confirmAccountDeletion(String token);
}