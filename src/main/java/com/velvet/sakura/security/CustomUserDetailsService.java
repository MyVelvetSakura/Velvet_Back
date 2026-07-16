package com.velvet.sakura.security;

import com.velvet.sakura.entity.Account;
import com.velvet.sakura.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String name) {
        Account account = accountRepository.findByName(name).stream()
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + name));

        return User.builder()
                .username(account.getName())
                .password(account.getPasswordHash())
                .authorities(List.of())
                .build();
    }
}
