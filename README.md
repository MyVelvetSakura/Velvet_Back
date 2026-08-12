# 🌸 Velvet Sakura — Backend

API REST en **Spring Boot** para *Velvet Sakura*, una web de tarot inspirada en Cardcaptor Sakura. Gestiona cuentas de usuario, mazos de cartas (Sakura y Clow), lecturas guardadas, interpretación mágica asistida por IA, y un sistema de progreso con experiencia, créditos y logros.

---

## ✨ Características

- **Autenticación JWT** con verificación de cuenta por email.
- **Recuperación de contraseña** mediante código de 6 dígitos, válido 5 minutos.
- **Eliminación de cuenta con doble confirmación**: solicitud con contraseña + enlace de confirmación enviado por email.
- **Alertas de seguridad**: tras 3 intentos de login fallidos, se envía un email con la IP y la ubicación aproximada del intento.
- **Dos mazos de cartas** (Sakura / Clow), cada uno con su propio catálogo de 53–55 cartas.
- **Interpretación con IA** (vía Groq): cada tirada de 3 cartas (pasado / presente / futuro) se interpreta en base a los significados reales, conectados con la pregunta del usuario o de forma libre.
- **Sistema de progreso**: experiencia por lectura, subida de nivel, créditos ("Plumas de Yue") canjeables por reinicios de tirada, y logros desbloqueables.
- **Emails transaccionales** con plantillas HTML (Thymeleaf) para verificación, recuperación, alertas y eliminación de cuenta.
- **Avatares personalizables** por catálogo de claves.

---

## 🛠️ Stack técnico

| Categoría | Tecnología |
|---|---|
| Framework | Spring Boot 4 |
| Lenguaje | Java 21 |
| Base de datos | PostgreSQL |
| Seguridad | Spring Security + JWT (jjwt) |
| Emails | Spring Mail + Thymeleaf |
| IA | Groq API (modelo Llama 3.3) |
| Geolocalización | ip-api.com |
| Build | Maven |
| Testing | JUnit 5, Mockito, MockMvc, H2 (in-memory) |

---

## 📁 Estructura del proyecto

```
src/main/java/com/velvet/sakura
├── config/          # Configuración (Security, CORS, HttpClient)
├── controller/       # Endpoints REST
├── dto/
│   ├── request/       # DTOs de entrada
│   └── response/       # DTOs de salida
├── entity/            # Entidades JPA
├── exception/          # Excepciones custom + GlobalExceptionHandler
├── repository/          # Repositorios Spring Data JPA
├── security/             # JWT, filtros, UserDetailsService
└── service/                # Lógica de negocio
```

---

## ⚙️ Configuración

Crea un archivo `.env` en la raíz del proyecto (excluido de git) con:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=nombredeDB
DB_USER=nombredetuUSERenpostgres
DB_PASS=tu_password

JWT_SECRET=tu_secret_en_base64_256_bits

SMTP_HOST=smtp.gmail.com
SMTP_PORT=465
SMTP_USERNAME=tu_correo@gmail.com
MAIL_PASS=tu_contraseña_de_aplicacion

GROQ_KEY=tu_api_key_de_groq

app.frontend-url=http://localhost:5173
```

> 💡 La contraseña de Gmail debe ser una **contraseña de aplicación** (no la contraseña normal de la cuenta), generada desde la configuración de seguridad de Google.

---

## 🚀 Puesta en marcha

```bash
# 1. Crea la base de datos en Postgres
createdb sakura

# 2. Compila y arranca
mvn clean install
mvn spring-boot:run
```

El servidor arranca en `http://localhost:3000`.

### Sembrar el catálogo de cartas

El repositorio incluye un script `seed_cards.sql` con las 108 filas de cartas (Sakura + Clow). Ejecútalo contra la base de datos antes de usar la app:

```bash
psql -U postgres -d sakura -f seed_cards.sql
```

---

## 🧪 Testing

```bash
mvn test
```

Cobertura incluida:
- **Servicios**: `AccountService`, `ProgressService`, `ReadingService`, `CardService`, `EmailService`, `GeoLocationService`, `OpenAIService`
- **Seguridad**: `JwtService`, `CustomUserDetailsService`, `JwtAuthenticationFilter`
- **Controllers** (`@WebMvcTest` + MockMvc): `AccountController`, `ReadingController`, `ProgressController`, `InterpretationController`, `CardController`
- **Repositorios** (`@DataJpaTest` + H2 en memoria): `AccountRepository`, `CardRepository`, `ReadingRepository`
- **GlobalExceptionHandler**

---

## 📡 Endpoints principales

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `POST` | `/api/accounts` | Registro de cuenta | No |
| `POST` | `/api/accounts/login` | Inicio de sesión | No |
| `GET` | `/api/accounts/verify` | Verificar cuenta por token | No |
| `POST` | `/api/accounts/forgot-password` | Solicitar código de recuperación | No |
| `POST` | `/api/accounts/reset-password` | Resetear contraseña con código | No |
| `PATCH` | `/api/accounts/{id}/avatar` | Cambiar avatar | Sí |
| `POST` | `/api/accounts/{id}/request-deletion` | Solicitar eliminación de cuenta | Sí |
| `GET` | `/api/accounts/confirm-deletion` | Confirmar eliminación | No |
| `GET` | `/api/cards` | Listar cartas por mazo | No |
| `POST` | `/api/readings` | Guardar una lectura | Sí |
| `GET` | `/api/readings?userId=` | Historial de lecturas | Sí |
| `POST` | `/api/interpretation` | Generar interpretación con IA | Sí |
| `GET` | `/api/progress/{accountId}` | Progreso del usuario | Sí |
| `GET` | `/api/progress/{accountId}/achievements` | Logros del usuario | Sí |
| `POST` | `/api/progress/{accountId}/spend-retry` | Gastar créditos para reiniciar tirada | Sí |

---

## 🔒 Seguridad

- Contraseñas hasheadas con **BCrypt**.
- Autenticación **stateless** con JWT (24h de validez).
- Nombres de usuario únicos, sin distinguir mayúsculas/minúsculas.
- Bloqueo silencioso + alerta por email tras 3 intentos fallidos de login.
- Enlaces de verificación/recuperación/eliminación con expiración y confirmación manual (evita consumo accidental por escáneres automáticos de email).


## Autora
Jennifer Cros Bañuelos

## Versión del proyecto
Velvet Sakura v 8.0.1
