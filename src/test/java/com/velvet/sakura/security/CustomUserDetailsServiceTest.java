package com.velvet.sakura.security;

import com.velvet.sakura.entity.Account;
import com.velvet.sakura.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_conCuentaExistente_devuelveUserDetailsCorrecto() {
        Account account = Account.builder()
                .id(1L)
                .name("Ragnarok1")
                .email("ragnarok1@gmail.com")
                .passwordHash("hashedPassword")
                .enabled(true)
                .build();

        when(accountRepository.findByName("Ragnarok1")).thenReturn(List.of(account));

        UserDetails userDetails = userDetailsService.loadUserByUsername("Ragnarok1");

        assertThat(userDetails.getUsername()).isEqualTo("Ragnarok1");
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsername_conCuentaInexistente_lanzaUsernameNotFoundException() {
        when(accountRepository.findByName("NoExiste")).thenReturn(List.of());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("NoExiste"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("NoExiste");
    }
}