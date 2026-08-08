package com.velvet.sakura.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velvet.sakura.dto.request.CreateAccountRequest;
import com.velvet.sakura.dto.request.LoginRequest;
import com.velvet.sakura.dto.response.AccountResponse;
import com.velvet.sakura.dto.response.AuthResponse;
import com.velvet.sakura.exception.ResourceNotFoundException;
import com.velvet.sakura.security.CustomUserDetailsService;
import com.velvet.sakura.security.JwtService;
import com.velvet.sakura.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.velvet.sakura.dto.request.RequestDeletionRequest;
import com.velvet.sakura.dto.request.UpdateAvatarRequest;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @MockitoBean
        private AccountService accountService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        @Test
        void register_conDatosValidos_devuelve201YLaCuentaCreada() throws Exception {
                CreateAccountRequest request = new CreateAccountRequest();
                request.setName("NuevaUsuaria");
                request.setEmail("nueva@gmail.com");
                request.setPassword("password123");

                AccountResponse response = new AccountResponse(1L, "NuevaUsuaria", "nueva@gmail.com", "default");
                when(accountService.createAccount(any())).thenReturn(response);

                mockMvc.perform(post("/api/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("NuevaUsuaria"))
                                .andExpect(jsonPath("$.email").value("nueva@gmail.com"));
        }

        @Test
        void register_sinNombre_devuelve400PorValidacion() throws Exception {
                CreateAccountRequest request = new CreateAccountRequest();
                request.setName("");
                request.setEmail("nueva@gmail.com");
                request.setPassword("password123");

                mockMvc.perform(post("/api/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void register_conEmailInvalido_devuelve400PorValidacion() throws Exception {
                CreateAccountRequest request = new CreateAccountRequest();
                request.setName("Usuaria");
                request.setEmail("esto-no-es-un-email");
                request.setPassword("password123");

                mockMvc.perform(post("/api/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void register_conContrasenaCortaMenorA8Caracteres_devuelve400() throws Exception {
                CreateAccountRequest request = new CreateAccountRequest();
                request.setName("Usuaria");
                request.setEmail("nueva@gmail.com");
                request.setPassword("1234");

                mockMvc.perform(post("/api/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void login_conCredencialesValidas_devuelve200YElToken() throws Exception {
                LoginRequest request = new LoginRequest();
                request.setName("Ragnarok1");
                request.setPassword("password123");

                AccountResponse account = new AccountResponse(1L, "Ragnarok1", "ragnarok1@gmail.com", "default");
                AuthResponse authResponse = new AuthResponse(account, "fake-jwt-token");

                when(accountService.login(any(), anyString())).thenReturn(authResponse);

                mockMvc.perform(post("/api/accounts/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                                .andExpect(jsonPath("$.account.name").value("Ragnarok1"));
        }

        @Test
        void login_conContrasenaIncorrecta_devuelve400ConMensaje() throws Exception {
                LoginRequest request = new LoginRequest();
                request.setName("Ragnarok1");
                request.setPassword("malaContraseña");

                when(accountService.login(any(), anyString()))
                                .thenThrow(new IllegalArgumentException("Contraseña incorrecta"));

                mockMvc.perform(post("/api/accounts/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Contraseña incorrecta"));
        }

        @Test
        void login_conUsuarioInexistente_devuelve404() throws Exception {
                LoginRequest request = new LoginRequest();
                request.setName("NoExiste");
                request.setPassword("password123");

                when(accountService.login(any(), anyString()))
                                .thenThrow(new ResourceNotFoundException("El usuario no existe"));

                mockMvc.perform(post("/api/accounts/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void login_conCuentaNoVerificada_devuelve409Conflict() throws Exception {
                LoginRequest request = new LoginRequest();
                request.setName("Ragnarok1");
                request.setPassword("password123");

                when(accountService.login(any(), anyString()))
                                .thenThrow(new IllegalStateException(
                                                "Debes verificar tu cuenta antes de iniciar sesión."));

                mockMvc.perform(post("/api/accounts/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        @Test
        void getByName_devuelveLaListaDeCoincidencias() throws Exception {
                AccountResponse account = new AccountResponse(1L, "Ragnarok1", "ragnarok1@gmail.com", "default");
                when(accountService.findByName("Ragnarok1")).thenReturn(java.util.List.of(account));

                mockMvc.perform(get("/api/accounts").param("name", "Ragnarok1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Ragnarok1"));
        }

        @Test
        void updateAvatar_conAvatarKeyValida_devuelve200ConLaCuentaActualizada() throws Exception {
                UpdateAvatarRequest request = new UpdateAvatarRequest();
                request.setAvatarKey("sakurachibi");

                AccountResponse response = new AccountResponse(1L, "Ragnarok1", "ragnarok1@gmail.com", "sakurachibi");
                when(accountService.updateAvatar(1L, "sakurachibi")).thenReturn(response);

                mockMvc.perform(patch("/api/accounts/1/avatar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.avatarKey").value("sakurachibi"));
        }

        @Test
        void updateAvatar_conAvatarKeyVacia_devuelve400PorValidacion() throws Exception {
                UpdateAvatarRequest request = new UpdateAvatarRequest();
                request.setAvatarKey(""); // @NotBlank falla

                mockMvc.perform(patch("/api/accounts/1/avatar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void updateAvatar_conCuentaInexistente_devuelve404() throws Exception {
                UpdateAvatarRequest request = new UpdateAvatarRequest();
                request.setAvatarKey("sakurachibi");

                when(accountService.updateAvatar(999L, "sakurachibi"))
                                .thenThrow(new ResourceNotFoundException("Cuenta no encontrada"));

                mockMvc.perform(patch("/api/accounts/999/avatar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void requestDeletion_conContrasenaCorrecta_devuelve200YEnviaElEmailDeConfirmacion() throws Exception {
                RequestDeletionRequest request = new RequestDeletionRequest();
                request.setPassword("password123");

                doNothing().when(accountService).requestAccountDeletion(1L, "password123");

                mockMvc.perform(post("/api/accounts/1/request-deletion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                verify(accountService).requestAccountDeletion(1L, "password123");
        }

        @Test
        void requestDeletion_conContrasenaIncorrecta_devuelve400() throws Exception {
                RequestDeletionRequest request = new RequestDeletionRequest();
                request.setPassword("contraseñaMala");

                doThrow(new IllegalArgumentException("Contraseña incorrecta"))
                                .when(accountService).requestAccountDeletion(1L, "contraseñaMala");

                mockMvc.perform(post("/api/accounts/1/request-deletion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void requestDeletion_conContrasenaVacia_devuelve400PorValidacion() throws Exception {
                RequestDeletionRequest request = new RequestDeletionRequest();
                request.setPassword(""); // @NotBlank falla

                mockMvc.perform(post("/api/accounts/1/request-deletion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void confirmDeletion_conTokenValido_devuelve200YEliminaLaCuenta() throws Exception {
                doNothing().when(accountService).confirmAccountDeletion("token-valido-123");

                mockMvc.perform(get("/api/accounts/confirm-deletion").param("token", "token-valido-123"))
                                .andExpect(status().isOk());

                verify(accountService).confirmAccountDeletion("token-valido-123");
        }

        @Test
        void confirmDeletion_conTokenCaducado_devuelve409Conflict() throws Exception {
                doThrow(new IllegalStateException("El enlace de eliminación ha caducado"))
                                .when(accountService).confirmAccountDeletion("token-caducado");

                mockMvc.perform(get("/api/accounts/confirm-deletion").param("token", "token-caducado"))
                                .andExpect(status().isConflict());
        }

        @Test
        void confirmDeletion_conTokenInvalido_devuelve400() throws Exception {
                doThrow(new IllegalArgumentException("Enlace de eliminación no válido"))
                                .when(accountService).confirmAccountDeletion("token-falso");

                mockMvc.perform(get("/api/accounts/confirm-deletion").param("token", "token-falso"))
                                .andExpect(status().isBadRequest());
        }
}