package com.velvet.sakura.service;

import java.util.List;

import com.velvet.sakura.dto.request.CreateAccountRequest;
import com.velvet.sakura.dto.request.LoginRequest;
import com.velvet.sakura.dto.response.AccountResponse;
import com.velvet.sakura.dto.response.AuthResponse;

public interface AccountService {
AccountResponse createAccount(CreateAccountRequest request);
    AuthResponse login(LoginRequest request);
    List<AccountResponse> findByName(String name);
    AccountResponse updateName(Long id, String newName);
    boolean nameExists(String name);
    boolean emailExists(String email);
}
