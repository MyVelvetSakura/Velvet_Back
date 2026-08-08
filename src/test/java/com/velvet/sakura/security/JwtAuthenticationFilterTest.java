package com.velvet.sakura.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpiarContextoDeSeguridadDespues() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_sinHeaderAuthorization_continuaLaCadenaSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_conHeaderSinPrefijoBearer_continuaLaCadenaSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("token-sin-bearer");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_conTokenValido_autenticaAlUsuarioYContinuaLaCadena() throws Exception {
        UserDetails userDetails = User.builder()
                .username("Ragnarok1")
                .password("hashedPassword")
                .authorities(List.of())
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer fake-jwt-token");
        when(jwtService.extractUsername("fake-jwt-token")).thenReturn("Ragnarok1");
        when(userDetailsService.loadUserByUsername("Ragnarok1")).thenReturn(userDetails);
        when(jwtService.isTokenValid("fake-jwt-token", "Ragnarok1")).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("Ragnarok1");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_conTokenInvalido_noAutenticaPeroContinuaLaCadena() throws Exception {
        UserDetails userDetails = User.builder()
                .username("Ragnarok1")
                .password("hashedPassword")
                .authorities(List.of())
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer token-caducado");
        when(jwtService.extractUsername("token-caducado")).thenReturn("Ragnarok1");
        when(userDetailsService.loadUserByUsername("Ragnarok1")).thenReturn(userDetails);
        when(jwtService.isTokenValid("token-caducado", "Ragnarok1")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_conCuentaBorradaTrasEmitirElToken_noAutenticaYContinuaLaCadenaSinExcepcion() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-de-cuenta-borrada");
        when(jwtService.extractUsername("token-de-cuenta-borrada")).thenReturn("UsuarioBorrado");
        when(userDetailsService.loadUserByUsername("UsuarioBorrado"))
                .thenThrow(new UsernameNotFoundException("Usuario no encontrado: UsuarioBorrado"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}