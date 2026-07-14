package com.velvet.sakura.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.velvet.sakura.dto.request.CreateAccountRequest;
import com.velvet.sakura.dto.request.LoginRequest;
import com.velvet.sakura.dto.response.AccountResponse;
import com.velvet.sakura.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    public AccountResponse login(@RequestBody LoginRequest request) {
        return accountService.login(request);
    }

    @GetMapping(params = "name")
    public List<AccountResponse> getByName(@RequestParam String name) {
        return accountService.findByName(name);
    }

    @PatchMapping("/{id}")
    public AccountResponse updateName(@PathVariable Long id, @RequestBody String newName) {
        return accountService.updateName(id, newName);
    }
}
