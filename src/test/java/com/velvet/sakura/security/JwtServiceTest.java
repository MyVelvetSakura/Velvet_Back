package com.velvet.sakura.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET =
            "c2VjcmV0b1NvbG9QYXJhVGVzdHNObk9Vc2FyRW5Qcm9kdWNjaW9uMTIzNDU2Nzg=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86_400_000L);
    }

    @Test
    void generateToken_devuelveUnTokenConTresPartesSeparadasPorPuntos() {
        String token = jwtService.generateToken("Ragnarok1");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_devuelveElNombreConQueSeGeneroElToken() {
        String token = jwtService.generateToken("Ragnarok1");

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("Ragnarok1");
    }

    @Test
    void isTokenValid_conTokenReciénGeneradoYMismoUsername_devuelveTrue() {
        String token = jwtService.generateToken("Ragnarok1");

        boolean result = jwtService.isTokenValid(token, "Ragnarok1");

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValid_conUsernameDistintoAlDelToken_devuelveFalse() {
        String token = jwtService.generateToken("Ragnarok1");

        boolean result = jwtService.isTokenValid(token, "OtroUsuario");

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_conTokenExpirado_devuelveFalse() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1L);
        String expiredToken = jwtService.generateToken("Ragnarok1");

        boolean result = jwtService.isTokenValid(expiredToken, "Ragnarok1");

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_conTokenMalformado_devuelveFalseSinLanzarExcepcion() {
        boolean result = jwtService.isTokenValid("esto-no-es-un-jwt-valido", "Ragnarok1");

        assertThat(result).isFalse();
    }

    @Test
    void extractUsername_conTokenFirmadoConOtraClave_lanzaExcepcion() {
        String token = jwtService.generateToken("Ragnarok1");

        ReflectionTestUtils.setField(jwtService, "secret",
                "b3RyYUNsYXZlU29sb1BhcmFUZXN0c05uT1VzYXJFblByb2R1Y2Npb24xMjM0NTY=");

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }
}