# apellido-post1-u9 · Seguridad web: sesión + JWT (Unidad 9)

Laboratorio de Spring Security con **dos mecanismos de autenticación que conviven en un mismo proyecto Maven** (`seguridad-web/`):

| | Parte 1 | Parte 2 |
|---|---|---|
| Mecanismo | Sesión HTTP (`form-login`) + CSRF | JWT firmado (HS256) en `Authorization: Bearer` |
| Rutas | Vistas web: `/login`, `/dashboard`, `/comentarios`, `/admin/**` | API REST: `/api/**` |
| Estado | Con estado (cookie `JSESSIONID`) | **Stateless** (`SessionCreationPolicy.STATELESS`) |
| Cadena | `webFilterChain` · `@Order(2)` | `apiFilterChain` · `@Order(1)` |

**Autor:** Juan David · Ingeniería de Sistemas, UFPS

---

## 1. Estructura

```
apellido-post1-u9/
├── README.md
├── capturas/                      ← evidencias (ver sección 5 y 6)
├── scripts/probar-api.sh          ← reproduce las pruebas de la API con curl
└── seguridad-web/                 ← proyecto Spring Boot 3.3.5 / Java 21
    └── src/main/java/com/ufps/seguridadweb/
        ├── config/       SecurityConfig, PasswordConfig (BCrypt), DataInitializer
        ├── controller/   Login, Dashboard, Comentario, Admin, Demo*  +  AuthRestController, TareaRestController
        ├── service/      CustomUserDetailsService, UsuarioService, ComentarioService, TareaService
        ├── repository/   UsuarioRepository, ComentarioRepository, TareaRepository (@Query), ConsultaVulnerableDao (solo demo)
        ├── model/        Usuario, Rol, Comentario, Tarea + DTOs (LoginRequest, TokenResponse, TareaRequest)
        └── security/     JwtUtil, JwtAuthFilter, JsonAuthenticationEntryPoint, JsonAccessDeniedHandler
```

Inyección por constructor en todas las clases, sin contraseñas ni claves dentro del código: viven en `application.properties` / variables de entorno.

## 2. Cómo ejecutarlo

Requisitos: JDK 21 y Maven 3.9+.

```bash
cd seguridad-web
mvn spring-boot:run          # http://localhost:8080
```

Usuarios de prueba (se guardan con BCrypt al arrancar):

| Usuario | Rol | Contraseña (por defecto, sobreescribible con `SEED_ADMIN_PASSWORD` / `SEED_USER_PASSWORD`) |
|---|---|---|
| `admin` | ADMIN | `Admin#2026` |
| `juan` | USER | `User#2026` |

Variables de entorno útiles:

| Variable | Para qué |
|---|---|
| `JWT_SECRET` | Clave de firma del JWT (mínimo 32 caracteres). |
| `JWT_EXPIRATION_MS` | Vida del token (por defecto 900000 = 15 min). Con `5000` se prueba un token expirado. |
| `DEMO_VULNERABLE=true` | Activa **solo para las capturas** los endpoints inseguros (`th:utext` y SQL concatenado). Por defecto está en `false`. |

> **Nota sobre la clave JWT:** `application.properties` trae un valor por defecto *solo para el laboratorio local*
> (`app.jwt.secret=${JWT_SECRET:...}`). En cualquier despliegue real se debe definir `JWT_SECRET` como variable de entorno.

## 3. Parte 1 · Autenticación con sesión

Qué implementa: `SecurityFilterChain` con `formLogin`, `UserDetailsService` que lee usuarios de la BD, `BCryptPasswordEncoder`, `/admin/**` solo para `ADMIN` (`hasRole` en `HttpSecurity` y `@PreAuthorize` en `AdminController`), CSRF activo, consultas con `@Query` parametrizado y Thymeleaf con `th:text` en todos los datos de usuario.

### 3.1 Login con sesión
![Login con sesión](capturas/01-login-sesion.png)

### 3.2 Dashboard con `th:text` (seguro) y vista según rol
![Dashboard con th:text](capturas/02-dashboard-th-text.png)

`admin` ve la tarjeta de administrador y `/admin/panel`; `juan` (USER) no ve el enlace y recibe 403 si entra a `/admin/panel` a mano.

### 3.3 XSS: `th:text` vs `th:utext`
Se publica en `/comentarios` el payload `<script>alert('XSS')</script>`.

- Con **`th:text`** (versión segura, `comentarios.html`) Thymeleaf escapa `<` y `>` y el navegador lo muestra como texto: **no se ejecuta**.
- Con **`th:utext`** (prueba temporal, `demo/xss-utext.html`, solo con `DEMO_VULNERABLE=true`) el HTML no se escapa y **el script se ejecuta**.

![XSS ejecutado con th:utext](capturas/03-xss-utext-ejecutado.png)

### 3.4 SQL Injection mitigado con `@Query`
`GET /demo/sqli-seguro?nombre=' OR '1'='1` usa `@Query("... WHERE u.username = :username")`: el valor viaja como parámetro, se trata como texto y la respuesta es `[]`.
Contraste (solo con `DEMO_VULNERABLE=true`): `/demo/sqli-inseguro?nombre=' OR '1'='1` concatena la entrada en el SQL y devuelve **todos** los usuarios.

![SQL Injection mitigado](capturas/04-sqli-mitigado-query.png)

### 3.5 CSRF rechazado (403)
Un POST autenticado por la cookie de sesión pero **sin** el token CSRF es rechazado. Desde la consola del navegador, con la sesión iniciada:

```js
fetch('/comentarios', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:'texto=sin-token'})
  .then(r => console.log(r.status))   // 403
```
![CSRF rechazado](capturas/05-csrf-rechazado-403.png)

## 4. Parte 2 · API REST con JWT

Qué implementa: `JwtUtil` (emite y valida con firma HMAC-SHA256 y expiración), `JwtAuthFilter extends OncePerRequestFilter` (lee el header, valida y puebla el `SecurityContext`), `apiFilterChain` independiente con `@Order(1)`, `STATELESS` y respuestas 401/403 en JSON, y los endpoints `POST /api/auth/login`, `GET/POST /api/tareas` y `GET /api/tareas/todas` (solo ADMIN).

Todo se reproduce con `scripts/probar-api.sh` o con estos comandos (en Windows PowerShell usar `curl.exe`):

```bash
# 1) Login: devuelve el JWT
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d '{"username":"juan","password":"User#2026"}'

# 2) Petición con token  -> 200
curl -i http://localhost:8080/api/tareas -H "Authorization: Bearer <TOKEN>"

# 3) Petición sin token  -> 401
curl -i http://localhost:8080/api/tareas

# 4) Token alterado (cambiar un carácter de la firma) -> 401
# 5) Token expirado: arrancar con JWT_EXPIRATION_MS=5000, esperar 6 s y repetir el paso 2 -> 401
# 6) Token de USER en /api/tareas/todas -> 403
```

### 4.1 `POST /api/auth/login` con el token recibido
![Login JWT](capturas/06-api-login-token.png)

### 4.2 `GET /api/tareas` autenticado (200)
![API autenticada](capturas/07-api-tareas-200.png)

### 4.3 `GET /api/tareas` sin token (401)
![API sin token](capturas/08-api-tareas-401.png)

### 4.4 Sesión y JWT en paralelo
Mientras la API responde con tokens, las vistas web de la Parte 1 siguen funcionando con sesión en el navegador (captura opcional `09-sesion-y-jwt-en-paralelo.png`).

## 5. ¿Por qué coexisten sesión y JWT?

Son dos casos de uso distintos y cada mecanismo se queda donde es más fuerte:

- **Sesión (web con navegador).** Es lo natural para páginas renderizadas en el servidor: el navegador guarda la cookie y la reenvía solo. Eso exige proteger contra **CSRF** (por eso va activo) y permite cerrar sesión de inmediato invalidándola en el servidor.
- **JWT (API para clientes que no son un navegador de páginas).** Apps móviles, SPAs, scripts u otros servicios llaman con `Authorization: Bearer`. El servidor no guarda estado: valida la firma y la expiración en cada petición, lo que escala mejor y no depende de cookies. Como el navegador **no adjunta el header por su cuenta**, aquí CSRF no aplica y se desactiva **solo** en esta cadena.

Cómo se evita que se estorben:

1. Dos `SecurityFilterChain` con `@Order` explícito. La de `/api/**` (`securityMatcher("/api/**")`, orden 1) se evalúa primero; todo lo demás cae en la web (orden 2).
2. `apiFilterChain` es `STATELESS`: no crea ni usa `HttpSession`, aunque el navegador envíe una cookie `JSESSIONID`.
3. `JwtAuthFilter` **no es `@Component`**: Spring Boot registraría automáticamente cualquier `Filter` como bean en todas las peticiones y se ejecutaría también en las rutas de sesión. Se instancia únicamente dentro de `apiFilterChain`.
4. Cada cadena tiene su propio manejo de errores: la web redirige a `/login`; la API responde 401/403 en JSON.

## 6. Commits

| Parte | Commit |
|---|---|
| 1 | `feat(parte1): dependencias Maven y modelo de dominio (Usuario, Rol, Comentario)` |
| 1 | `feat(parte1): repositorios con @Query parametrizado, servicios y BCrypt para las contrasenas` |
| 1 | `feat(parte1): SecurityConfig de sesion, controladores y vistas Thymeleaf (th:text, roles, CSRF)` |
| 2 | `feat(parte2): agrega dependencia jjwt y propiedades del token (clave por variable de entorno)` |
| 2 | `feat(parte2): JwtUtil (emision/validacion firmada con expiracion) y JwtAuthFilter (OncePerRequestFilter)` |
| 2 | `feat(parte2): apiFilterChain stateless con @Order(1), login JWT y endpoints REST /api/tareas` |
| docs | `docs: README con evidencias y explicacion de sesion + JWT` |
