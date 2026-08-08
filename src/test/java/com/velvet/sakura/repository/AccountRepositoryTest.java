package com.velvet.sakura.repository;

import com.velvet.sakura.entity.Account;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private AccountRepository accountRepository;

    @Test
    void existsByNameIgnoreCase_conMismoNombreDistintasMayusculas_devuelveTrue() {
        Account account = Account.builder()
                .name("Ragnarok1")
                .email("ragnarok1@gmail.com")
                .passwordHash("hash")
                .enabled(true)
                .avatarKey("default")
                .build();
        accountRepository.save(account);

        boolean existsExacto = accountRepository.existsByNameIgnoreCase("Ragnarok1");
        boolean existsMinusculas = accountRepository.existsByNameIgnoreCase("ragnarok1");
        boolean existsMayusculas = accountRepository.existsByNameIgnoreCase("RAGNAROK1");

        assertThat(existsExacto).isTrue();
        assertThat(existsMinusculas).isTrue();
        assertThat(existsMayusculas).isTrue();
    }

    @Test
    void existsByNameIgnoreCase_conNombreDistinto_devuelveFalse() {
        Account account = Account.builder()
                .name("Ragnarok1")
                .email("ragnarok1@gmail.com")
                .passwordHash("hash")
                .enabled(true)
                .avatarKey("default")
                .build();
        accountRepository.save(account);

        boolean exists = accountRepository.existsByNameIgnoreCase("OtroNombre");

        assertThat(exists).isFalse();
    }

    @Test
    void findByNameIgnoreCase_conNombreEnDistintasMayusculas_encuentraLaCuenta() {
        Account account = Account.builder()
                .name("Jenny")
                .email("jenny@gmail.com")
                .passwordHash("hash")
                .enabled(true)
                .avatarKey("default")
                .build();
        accountRepository.save(account);

        Optional<Account> result = accountRepository.findByNameIgnoreCase("JENNY");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("jenny@gmail.com");
    }

    @Test
    void findByNameIgnoreCase_conNombreInexistente_devuelveOptionalVacio() {
        Optional<Account> result = accountRepository.findByNameIgnoreCase("NoExiste");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_conEmailYaRegistrado_devuelveTrue() {
        Account account = Account.builder()
                .name("Usuaria")
                .email("test@gmail.com")
                .passwordHash("hash")
                .enabled(true)
                .avatarKey("default")
                .build();
        accountRepository.save(account);

        boolean exists = accountRepository.existsByEmail("test@gmail.com");

        assertThat(exists).isTrue();
    }

    @Test
    void findByEmail_conEmailExistente_devuelveLaCuenta() {
        Account account = Account.builder()
                .name("Usuaria")
                .email("busqueda@gmail.com")
                .passwordHash("hash")
                .enabled(true)
                .avatarKey("default")
                .build();
        accountRepository.save(account);

        Optional<Account> result = accountRepository.findByEmail("busqueda@gmail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Usuaria");
    }

    @Test
    void findByName_devuelveListaDeCoincidenciasExactas() {
        Account account = Account.builder()
                .name("Ragnarok1")
                .email("ragnarok1@gmail.com")
                .passwordHash("hash")
                .enabled(true)
                .avatarKey("default")
                .build();
        accountRepository.save(account);

        List<Account> result = accountRepository.findByName("Ragnarok1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Ragnarok1");
    }

    @Test
    void save_conNombreDuplicado_lanzaExcepcionPorConstraintUnique() {
        Account account1 = Account.builder()
                .name("Duplicado")
                .email("uno@gmail.com")
                .passwordHash("hash")
                .enabled(true)
                .avatarKey("default")
                .build();
        accountRepository.saveAndFlush(account1);

        Account account2 = Account.builder()
                .name("Duplicado")
                .email("dos@gmail.com")
                .passwordHash("hash")
                .enabled(true)
                .avatarKey("default")
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> accountRepository.saveAndFlush(account2)
        );
    }
}