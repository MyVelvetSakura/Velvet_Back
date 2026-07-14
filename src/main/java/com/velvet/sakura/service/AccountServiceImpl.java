package com.velvet.sakura.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.velvet.sakura.dto.request.CreateAccountRequest;
import com.velvet.sakura.dto.request.LoginRequest;
import com.velvet.sakura.dto.response.AccountResponse;
import com.velvet.sakura.entity.Account;
import com.velvet.sakura.exception.ResourceNotFoundException;
import com.velvet.sakura.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
private final AccountRepository accountRepository;
private final BCryptPasswordEncoder passwordEncoder;

@Override
public AccountResponse createAccount(CreateAccountRequest request){
    if(accountRepository.existsByName(request.getName())){
        throw new IllegalArgumentException("El nombre ya está registrado");
    }
    if(accountRepository.existsByEmail(request.getEmail())){
        throw new IllegalArgumentException("El email ya está registrado");
    }

    Account account = Account.builder()
    .name(request.getName())
    .email(request.getEmail())
    .passwordHash(passwordEncoder.encode(request.getPassword()))
    .build();

    return toResponse(accountRepository.save(account));
}

@Override
public AccountResponse login(LoginRequest request){
    Account account=accountRepository.findByName(request.getName()).stream().findFirst().orElseThrow(()->new ResourceNotFoundException("El usuario no existe"));
    if(!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())){
        throw new IllegalArgumentException("Contraseña incorrecta");
    }
    return toResponse(account);
}

@Override
public List<AccountResponse> findByName(String name){
    return accountRepository.findByName(name).stream().map(this::toResponse).toList();
}

@Override
public AccountResponse updateName(Long id,String newName){
    Account account=accountRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Cuenta no encontrada"));
    account.setName(newName);
    return toResponse(accountRepository.save(account));
}

@Override
public boolean nameExists(String name){
    return accountRepository.existsByName(name);
}

@Override
public boolean emailExists(String email){
    return accountRepository.existsByEmail(email);
}

private AccountResponse toResponse(Account account){
    return new AccountResponse(account.getId(),account.getName(),account.getEmail());
}
}
