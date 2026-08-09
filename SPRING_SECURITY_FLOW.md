# Spring Security + JWT - Flujo completo del proyecto

Este documento analiza unicamente la parte de Spring Security y JWT del proyecto.

Punto importante: el prompt menciona `POST /login`, pero en el codigo actual del proyecto el endpoint real es:

```text
POST /api/v1/auth/login
```

Eso sale de:

```java
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest){
        return ResponseEntity.ok(authService.login(authRequest));
    }
}
```

Ruta: `src/main/java/com/spring_security/course_spring_security/controller/AuthController.java`

---

## 1. Mapa general

### Mapa de autenticacion por usuario y password

```text
POST /api/v1/auth/login
        |
        v
SecurityFilterChain
        |
        v
JwtAuthenticationFilter
        |
        |-- Sin Authorization Bearer -> continua
        v
AuthController.login(...)
        |
        v
AuthService.login(...)
        |
        v
AuthenticationManager.authenticate(...)
        |
        v
ProviderManager
        |
        v
DaoAuthenticationProvider
        |
        v
CustomUserDetailsService.loadUserByUsername(email)
        |
        v
EmployeeRepository.findByEmail(email)
        |
        v
UserDetails
        |
        v
PasswordEncoder.matches(rawPassword, encodedPassword)
        |
        v
Authentication autenticado
        |
        v
JwtService.generateToken(userDetails)
        |
        v
AuthResponse(token, "Bearer", expiresIn)
```

### Mapa de una request posterior con JWT

```text
GET /api/v1/employee
Authorization: Bearer xxx
        |
        v
SecurityFilterChain
        |
        v
JwtAuthenticationFilter
        |
        v
JwtService.extractUsername(token)
        |
        v
CustomUserDetailsService.loadUserByUsername(email)
        |
        v
JwtService.isTokenValid(token, userDetails)
        |
        v
UsernamePasswordAuthenticationToken(userDetails, null, authorities)
        |
        v
SecurityContextHolder.getContext().setAuthentication(...)
        |
        v
Authorization de Spring Security
        |
        v
Controller protegido
```

---

## 2. Componentes involucrados

| Componente | Ruta / clase | Responsabilidad | Tipo |
|---|---|---|---|
| `SecurityConfig` | `src/main/java/com/spring_security/course_spring_security/security/SecurityConfig.java` | Configura filtros, endpoints publicos/protegidos, provider, manager y encoder. | Mi codigo |
| `AuthController` | `src/main/java/com/spring_security/course_spring_security/controller/AuthController.java` | Recibe `POST /api/v1/auth/login`. | Mi codigo |
| `AuthService` | `src/main/java/com/spring_security/course_spring_security/service/AuthService.java` | Llama a `AuthenticationManager` y genera el JWT. | Mi codigo |
| `AuthRequest` | `src/main/java/com/spring_security/course_spring_security/dto/auth/AuthRequest.java` | DTO de entrada con `email` y `password`. | Mi codigo |
| `AuthResponse` | `src/main/java/com/spring_security/course_spring_security/service/AuthResponse.java` | DTO de salida con token, tipo y expiracion. | Mi codigo |
| `JwtService` | `src/main/java/com/spring_security/course_spring_security/security/JwtService.java` | Crea, firma, lee y valida JWT. | Mi codigo |
| `JwtAuthenticationFilter` | `src/main/java/com/spring_security/course_spring_security/security/JwtAuthenticationFilter.java` | Filtro custom que autentica requests con Bearer Token. | Mi codigo + extension de Spring |
| `CustomUserDetailsService` | `src/main/java/com/spring_security/course_spring_security/security/CustomUserDetailsService.java` | Carga usuario por email y lo convierte a `UserDetails`. | Mi codigo + contrato de Spring |
| `RestAuthenticationEntryPoint` | `src/main/java/com/spring_security/course_spring_security/security/RestAuthenticationEntryPoint.java` | Devuelve JSON cuando falta autenticacion o el token no sirve. | Mi codigo + contrato de Spring |
| `RestAccessDeniedHandler` | `src/main/java/com/spring_security/course_spring_security/security/RestAccessDeniedHandler.java` | Devuelve JSON cuando hay usuario autenticado pero sin permiso. | Mi codigo + contrato de Spring |
| `EmployeeRepository` | `src/main/java/com/spring_security/course_spring_security/repository/EmployeeRepository.java` | Busca el usuario por email para autenticar. | Mi codigo usado por seguridad |
| `AuthenticationManager` | `org.springframework.security.authentication.AuthenticationManager` | Punto de entrada para autenticar credenciales. | Interface Spring Security |
| `ProviderManager` | `org.springframework.security.authentication.ProviderManager` | Implementacion usual de `AuthenticationManager`. Delega a providers. | Interno Spring Security |
| `AuthenticationProvider` | `org.springframework.security.authentication.AuthenticationProvider` | Contrato que sabe autenticar un tipo de token. | Interface Spring Security |
| `DaoAuthenticationProvider` | `org.springframework.security.authentication.dao.DaoAuthenticationProvider` | Valida usuario/password usando `UserDetailsService` y `PasswordEncoder`. | Implementacion Spring Security |
| `UserDetailsService` | `org.springframework.security.core.userdetails.UserDetailsService` | Contrato para cargar usuario por username. | Interface Spring Security |
| `UserDetails` | `org.springframework.security.core.userdetails.UserDetails` | Modelo de usuario que entiende Spring Security. | Interface Spring Security |
| `GrantedAuthority` | `org.springframework.security.core.GrantedAuthority` | Permiso/rol del usuario. | Interface Spring Security |
| `PasswordEncoder` | `org.springframework.security.crypto.password.PasswordEncoder` | Compara password plano con hash guardado. | Interface Spring Security |
| `BCryptPasswordEncoder` | `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder` | Encoder concreto usado en el proyecto. | Implementacion Spring Security |
| `SecurityFilterChain` | `org.springframework.security.web.SecurityFilterChain` | Cadena de filtros que procesa cada request. | Spring Security |
| `OncePerRequestFilter` | `org.springframework.web.filter.OncePerRequestFilter` | Base para ejecutar un filtro una vez por request. | Spring Framework |
| `SecurityContextHolder` | `org.springframework.security.core.context.SecurityContextHolder` | Guarda el `Authentication` de la request actual. | Spring Security |
| `UsernamePasswordAuthenticationFilter` | `org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter` | Filtro de login form tradicional. Esta desactivado en la practica por `formLogin().disable()`, pero se usa como punto de posicion para insertar el JWT filter antes. | Spring Security |

---

## 3. Flujo del LOGIN paso a paso

### Paso 1 - Entra la request HTTP

**Estamos en:**
`Cliente HTTP -> POST /api/v1/auth/login`

**Codigo relevante:**

```java
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest){
        return ResponseEntity.ok(authService.login(authRequest));
    }
}
```

**Que ocurre tecnicamente?**
La request entra al servidor en la ruta `POST /api/v1/auth/login`. Antes de llegar al controller, Spring Security intercepta la request con su `SecurityFilterChain`.

**En palabras simples:**
Aunque el controller sea el destino final, Spring Security revisa la peticion primero.

**Quien ejecuta esto?**
`SPRING SECURITY`

**A donde pasa despues?**
`HTTP Request -> SecurityFilterChain`

---

### Paso 2 - La request atraviesa `SecurityFilterChain`

**Estamos en:**
`SecurityConfig -> securityFilterChain(HttpSecurity http)`

**Codigo relevante:**

```java
@Bean
public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
     http
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout -> logout.disable())
            .authorizeHttpRequests(
                    auth -> auth
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .requestMatchers("/api/v1/rol/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/employee/**").hasAnyRole("ADMIN", "USER")
                            .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

**Que ocurre tecnicamente?**
Spring usa este bean para construir la cadena de seguridad. La ruta `/api/v1/auth/**` esta marcada como `permitAll()`, por eso el login no necesita estar autenticado previamente.

**En palabras simples:**
El login esta en la lista de puertas publicas. Cualquiera puede intentar autenticarse.

**Quien ejecuta esto?**
`SPRING SECURITY`

**A donde pasa despues?**
`SecurityFilterChain -> JwtAuthenticationFilter.doFilterInternal(...)`

---

### Paso 3 - Se ejecuta `JwtAuthenticationFilter` tambien en login

**Estamos en:**
`JwtAuthenticationFilter -> doFilterInternal(...)`

**Codigo relevante:**

```java
final String authHeader = request.getHeader("Authorization");
if(authHeader == null || !authHeader.startsWith("Bearer ")){
    filterChain.doFilter(request,response);
    return;
}
```

**Que ocurre tecnicamente?**
El filtro custom se ejecuta porque fue agregado a la cadena con:

```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

En un login normal no existe header `Authorization: Bearer ...`. Entonces el filtro no intenta validar JWT y llama a `filterChain.doFilter(request,response)`.

**En palabras simples:**
El filtro pregunta: "Traes token?". Como el login todavia no tiene token, responde: "Entonces sigue tu camino".

**Quien ejecuta esto?**
`SPRING SECURITY`

**A donde pasa despues?**
`JwtAuthenticationFilter.doFilterInternal(...) -> siguientes filtros -> AuthController.login(...)`

---

### Paso 4 - Llega al controller

**Estamos en:**
`AuthController -> login(AuthRequest authRequest)`

**Codigo relevante:**

```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest){
    return ResponseEntity.ok(authService.login(authRequest));
}
```

**Que ocurre tecnicamente?**
Spring MVC convierte el JSON a un objeto `AuthRequest` y valida sus anotaciones.

```java
public class AuthRequest {
    @NotBlank(message = "The email is incorrect")
    @Email(message = "The email format is invalid")
    private String email;

    @NotBlank(message = "The password is incorrect")
    private String password;
}
```

Parametros recibidos:

```json
{
  "email": "usuario@gmail.com",
  "password": "123456"
}
```

Objeto creado:

```text
AuthRequest
email = usuario@gmail.com
password = 123456
```

**En palabras simples:**
El JSON se transforma en un objeto Java con email y password.

**Quien ejecuta esto?**
`SPRING MVC` y luego `MI CODIGO`

**A donde pasa despues?**
`AuthController.login(...) -> AuthService.login(authRequest)`

---

### Paso 5 - `AuthService` crea el token de autenticacion inicial

**Estamos en:**
`AuthService -> login(AuthRequest authRequest)`

**Codigo relevante:**

```java
Authentication authentication = authenticationManager.authenticate(
  new UsernamePasswordAuthenticationToken(
          authRequest.getEmail(),
          authRequest.getPassword()
  )
);
```

**Que ocurre tecnicamente?**
Aqui se crea un `UsernamePasswordAuthenticationToken` inicial.

Contenido antes de autenticar:

```text
principal = authRequest.getEmail()
credentials = authRequest.getPassword()
authorities = vacio
authenticated = false
```

Ese objeto representa "alguien dice ser este email y trae esta password".

**En palabras simples:**
Tu codigo arma una credencial temporal. Todavia no esta probada.

**Quien ejecuta esto?**
`MI CODIGO`

**A donde pasa despues?**
`AuthService.login(...) -> AuthenticationManager.authenticate(...)`

---

### Paso 6 - Entra `AuthenticationManager`

**Estamos en:**
`AuthenticationManager -> authenticate(Authentication authentication)`

**Codigo relevante del proyecto:**

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration){
    return authenticationConfiguration.getAuthenticationManager();
}
```

**Que ocurre tecnicamente?**
`AuthenticationManager` es una interface de Spring Security. En este proyecto tu bean lo obtiene desde `AuthenticationConfiguration`.

`AuthenticationConfiguration` no lo escribiste tu. Lo crea Spring Boot / Spring Security como parte de la autoconfiguracion. Puedes recibirlo como parametro porque es un bean registrado en el contexto de Spring.

Cuando llamas:

```java
authenticationConfiguration.getAuthenticationManager()
```

Spring construye/devuelve el `AuthenticationManager` configurado para la aplicacion. Normalmente la implementacion es `ProviderManager`, que contiene una lista de `AuthenticationProvider`.

Como en `SecurityConfig` registraste:

```java
.authenticationProvider(authenticationProvider())
```

ese manager conoce tu `DaoAuthenticationProvider`.

**En palabras simples:**
Tu no construyes todo el motor a mano. Le dices a Spring: "Dame el manager que ya configuraste con mis piezas".

**Quien ejecuta esto?**
`MI CODIGO` llama a la interface. La implementacion la ejecuta `SPRING SECURITY`.

**A donde pasa despues?**
`AuthenticationManager.authenticate(...) -> ProviderManager.authenticate(...)`

---

### Paso 7 - `ProviderManager` busca un provider compatible

**Estamos en:**
`ProviderManager -> authenticate(...)`

**Codigo relevante del proyecto:**

```java
@Bean
AuthenticationProvider authenticationProvider(){
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
}
```

**Que ocurre tecnicamente?**
Esto es interno de Spring Security. `ProviderManager` recorre sus `AuthenticationProvider` y pregunta si alguno soporta el tipo de objeto recibido.

El objeto recibido es:

```text
UsernamePasswordAuthenticationToken
```

`DaoAuthenticationProvider` soporta ese tipo de autenticacion. Internamente ocurre algo equivalente a:

```text
ProviderManager.authenticate(token)
    -> DaoAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class)
    -> DaoAuthenticationProvider.authenticate(token)
```

**En palabras simples:**
El manager pregunta: "Quien sabe validar email y password?". `DaoAuthenticationProvider` responde: "Yo".

**Quien ejecuta esto?**
`INTERNO DE SPRING SECURITY`

**A donde pasa despues?**
`ProviderManager.authenticate(...) -> DaoAuthenticationProvider.authenticate(...)`

---

### Paso 8 - `DaoAuthenticationProvider` carga el usuario

**Estamos en:**
`DaoAuthenticationProvider -> authenticate(...)`

**Codigo relevante del proyecto:**

```java
DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
authenticationProvider.setPasswordEncoder(passwordEncoder());
```

**Que ocurre tecnicamente?**
`DaoAuthenticationProvider` necesita buscar el usuario real. Para eso usa el `UserDetailsService` que le pasaste en el constructor.

En tu proyecto, el bean concreto que implementa `UserDetailsService` es:

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ...
    }
}
```

Spring inyecta ese bean en `SecurityConfig` porque el constructor pide:

```java
UserDetailsService userDetailsService
```

**En palabras simples:**
El provider no sabe ir a tu base de datos por si solo. Usa tu clase `CustomUserDetailsService`.

**Quien ejecuta esto?**
`SPRING SECURITY`

**A donde pasa despues?**
`DaoAuthenticationProvider.authenticate(...) -> CustomUserDetailsService.loadUserByUsername(email)`

---

### Paso 9 - Tu `CustomUserDetailsService` busca el usuario

**Estamos en:**
`CustomUserDetailsService -> loadUserByUsername(String email)`

**Codigo relevante:**

```java
Employee employee = employeeRepository.findByEmail(email).orElseThrow(
        () -> new UsernameNotFoundException("Employee not found.")
);
```

**Que ocurre tecnicamente?**
El metodo recibe como parametro el email que venia en el `UsernamePasswordAuthenticationToken` inicial.

Parametro:

```text
email = usuario@gmail.com
```

Llama al repositorio:

```java
Optional<Employee> findByEmail(String email);
```

Si no encuentra empleado, lanza `UsernameNotFoundException`.

**En palabras simples:**
Busca en la base de datos si existe alguien con ese email.

**Quien ejecuta esto?**
`MI CODIGO`, llamado por `SPRING SECURITY`

**A donde pasa despues?**
`CustomUserDetailsService.loadUserByUsername(...) -> EmployeeRepository.findByEmail(...)`

---

### Paso 10 - Se construyen authorities/roles

**Estamos en:**
`CustomUserDetailsService -> loadUserByUsername(String email)`

**Codigo relevante:**

```java
List<GrantedAuthority> authorities = employee.getRoles().stream()
        .map(rol -> rol.getName().trim().toUpperCase(Locale.ROOT))
        .map(roleName -> roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName)
        .distinct()
        .map(SimpleGrantedAuthority::new)
        .map(authority -> (GrantedAuthority) authority)
        .toList();
```

**Que ocurre tecnicamente?**
Los roles del `Employee` se convierten a objetos `GrantedAuthority`.

Ejemplo:

```text
Rol en base de datos: ADMIN
GrantedAuthority: ROLE_ADMIN
```

Esto importa porque en `SecurityConfig` usas:

```java
.requestMatchers("/api/v1/rol/**").hasRole("ADMIN")
.requestMatchers("/api/v1/employee/**").hasAnyRole("ADMIN", "USER")
```

`hasRole("ADMIN")` busca internamente una authority llamada `ROLE_ADMIN`.

**En palabras simples:**
Tus roles se traducen al idioma que Spring Security entiende.

**Quien ejecuta esto?**
`MI CODIGO`, llamado por `SPRING SECURITY`

**A donde pasa despues?**
`CustomUserDetailsService.loadUserByUsername(...) -> User.builder()`

---

### Paso 11 - Se crea el `UserDetails`

**Estamos en:**
`CustomUserDetailsService -> loadUserByUsername(String email)`

**Codigo relevante:**

```java
return User.builder()
        .username(employee.getEmail())
        .password(employee.getPassword())
        .authorities(authorities)
        .build();
```

**Que ocurre tecnicamente?**
Se devuelve un objeto `UserDetails`. En este proyecto se usa la clase `User` de Spring Security.

Contenido:

```text
username = employee.getEmail()
password = employee.getPassword()
authorities = ROLE_ADMIN, ROLE_USER, etc.
```

Importante: `password` aqui debe ser el hash guardado, no la password plana.

**En palabras simples:**
Spring recibe una ficha completa del usuario: quien es, cual es su password cifrada y que roles tiene.

**Quien ejecuta esto?**
`MI CODIGO`

**A donde pasa despues?**
`CustomUserDetailsService.loadUserByUsername(...) -> DaoAuthenticationProvider`

---

### Paso 12 - Se compara la password

**Estamos en:**
`DaoAuthenticationProvider -> additionalAuthenticationChecks(...)`

**Codigo relevante del proyecto:**

```java
@Bean
public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder(12);
}
```

**Que ocurre tecnicamente?**
Esto ocurre dentro de Spring Security. `DaoAuthenticationProvider` compara:

```text
password plana recibida en el login
vs
password hasheada del UserDetails
```

La comparacion real la hace el `PasswordEncoder`, conceptualmente:

```text
PasswordEncoder.matches(rawPassword, encodedPassword)
```

En este proyecto el encoder concreto es `BCryptPasswordEncoder(12)`.

**En palabras simples:**
Spring no desencripta nada. Toma la password escrita por el usuario y verifica si corresponde al hash guardado.

**Quien ejecuta esto?**
`INTERNO DE SPRING SECURITY`

**A donde pasa despues?**
`DaoAuthenticationProvider.additionalAuthenticationChecks(...) -> resultado de autenticacion`

---

### Paso 13 - Si las credenciales son correctas, Spring devuelve `Authentication`

**Estamos en:**
`DaoAuthenticationProvider / ProviderManager -> authenticate(...)`

**Codigo relevante del proyecto que recibe el resultado:**

```java
Authentication authentication = authenticationManager.authenticate(
  new UsernamePasswordAuthenticationToken(
          authRequest.getEmail(),
          authRequest.getPassword()
  )
);
```

**Que ocurre tecnicamente?**
El `Authentication` inicial cambia.

Antes:

```text
UsernamePasswordAuthenticationToken
principal = email
credentials = password
authenticated = false
authorities = vacio
```

Despues:

```text
Authentication / UsernamePasswordAuthenticationToken autenticado
principal = UserDetails
credentials = normalmente null o protegidas
authenticated = true
authorities = authorities del UserDetails
```

El cambio ocurre dentro de `DaoAuthenticationProvider` cuando termina de validar usuario y password correctamente, y devuelve un `Authentication` autenticado al `ProviderManager`.

**En palabras simples:**
La credencial deja de ser "alguien dice ser" y pasa a ser "Spring comprobo que si es".

**Quien ejecuta esto?**
`INTERNO DE SPRING SECURITY`

**A donde pasa despues?**
`DaoAuthenticationProvider -> ProviderManager -> AuthService.login(...)`

---

### Paso 14 - `AuthService` extrae el `UserDetails`

**Estamos en:**
`AuthService -> login(AuthRequest authRequest)`

**Codigo relevante:**

```java
UserDetails userDetails = (UserDetails) authentication.getPrincipal();
```

**Que ocurre tecnicamente?**
El `principal` del `Authentication` autenticado ya no es solamente el string email. Ahora es el `UserDetails` devuelto por `CustomUserDetailsService`.

**En palabras simples:**
Tu servicio toma la ficha autenticada del usuario para crear el token.

**Quien ejecuta esto?**
`MI CODIGO`

**A donde pasa despues?**
`AuthService.login(...) -> JwtService.generateToken(userDetails)`

---

### Paso 15 - Se genera el JWT

**Estamos en:**
`JwtService -> generateToken(UserDetails userDetails)`

**Codigo relevante:**

```java
Instant now = Instant.now(clock);
List<String> authorities = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

return Jwts.builder()
        .issuer(ISSUER)
        .subject(userDetails.getUsername())
        .claim("authorities", authorities)
        .issuedAt(Date.from(now))
        .expiration(
                Date.from(now.plusMillis(jwtExpiration))
        )
        .signWith(getSigningKey())
        .compact();
```

**Que ocurre tecnicamente?**
El token se crea con:

```text
issuer = course-spring-security
subject = userDetails.getUsername()    // email
authorities = roles del usuario
issuedAt = fecha actual UTC
expiration = fecha actual + security.jwt.expiration-time
firma = clave HMAC obtenida desde security.jwt.secret-key
```

La clave sale de:

```java
@Value("${security.jwt.secret-key}")
private String secretKey;
```

y se transforma asi:

```java
byte[] keyBytes = Decoders.BASE64.decode(secretKey);
return Keys.hmacShaKeyFor(keyBytes);
```

**En palabras simples:**
El JWT es una tarjeta firmada: dice quien eres, que roles tienes, cuando fue emitida y cuando vence.

**Quien ejecuta esto?**
`MI CODIGO`

**A donde pasa despues?**
`JwtService.generateToken(...) -> AuthService.login(...)`

---

### Paso 16 - Se devuelve la respuesta HTTP

**Estamos en:**
`AuthService -> AuthController -> HTTP Response`

**Codigo relevante:**

```java
return new AuthResponse(token, "Bearer", jwtService.getJwtExpiration());
```

```java
return ResponseEntity.ok(authService.login(authRequest));
```

**Que ocurre tecnicamente?**
`AuthService` devuelve un `AuthResponse`:

```java
public class AuthResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
}
```

La respuesta final es `HTTP 200 OK` con un JSON parecido a:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000
}
```

**En palabras simples:**
Si el login fue correcto, el backend entrega el token que el cliente debera mandar en las siguientes peticiones.

**Quien ejecuta esto?**
`MI CODIGO` y serializacion HTTP de `SPRING MVC`

**A donde pasa despues?**
`AuthController.login(...) -> Cliente HTTP`

---

## 4. Flujo interno de AuthenticationManager

```text
MI CODIGO
AuthService.login(...)
        |
        v
authenticationManager.authenticate(token)
        |
        v
INTERNO DE SPRING SECURITY
ProviderManager.authenticate(token)
        |
        v
Busca AuthenticationProvider compatible
        |
        v
DaoAuthenticationProvider.supports(UsernamePasswordAuthenticationToken)
        |
        v
DaoAuthenticationProvider.authenticate(token)
        |
        v
CustomUserDetailsService.loadUserByUsername(email)
        |
        v
UserDetails
        |
        v
PasswordEncoder.matches(rawPassword, encodedPassword)
        |
        v
Authentication autenticado
```

### Transicion 1: `AuthService` -> `AuthenticationManager`

Tecnico: tu codigo llama directamente:

```java
authenticationManager.authenticate(
  new UsernamePasswordAuthenticationToken(email, password)
);
```

Simple: entregas email y password al motor de autenticacion.

### Transicion 2: `AuthenticationManager` -> `ProviderManager`

Tecnico: `AuthenticationManager` es una interface. La implementacion usada por Spring Security normalmente es `ProviderManager`.

Simple: llamas a una puerta generica; detras esta el manager real.

### Transicion 3: `ProviderManager` -> `DaoAuthenticationProvider`

Tecnico: `ProviderManager` revisa sus providers y selecciona uno compatible con `UsernamePasswordAuthenticationToken`.

Simple: busca quien sabe validar usuario/password.

### Transicion 4: `DaoAuthenticationProvider` -> `UserDetailsService`

Tecnico: `DaoAuthenticationProvider` usa el `UserDetailsService` recibido en:

```java
new DaoAuthenticationProvider(userDetailsService)
```

Simple: le pide a tu clase que cargue el usuario desde la base de datos.

### Transicion 5: `UserDetailsService` -> `UserDetails`

Tecnico: `CustomUserDetailsService` devuelve:

```java
User.builder()
        .username(employee.getEmail())
        .password(employee.getPassword())
        .authorities(authorities)
        .build();
```

Simple: devuelve un usuario en formato Spring Security.

### Transicion 6: `DaoAuthenticationProvider` -> `PasswordEncoder`

Tecnico: compara credenciales con:

```text
PasswordEncoder.matches(rawPassword, encodedPassword)
```

Simple: verifica si la password escrita coincide con el hash guardado.

---

## 5. Creacion del Authentication

### Antes de autenticar

Se crea aqui:

```java
new UsernamePasswordAuthenticationToken(
        authRequest.getEmail(),
        authRequest.getPassword()
)
```

Estado:

```text
Clase: UsernamePasswordAuthenticationToken
principal: email
credentials: password plana
authorities: vacio
authenticated: false
```

Este objeto existe dentro de `AuthService.login(...)` antes de llamar al manager.

### Despues de autenticar

Se recibe aqui:

```java
Authentication authentication = authenticationManager.authenticate(...);
```

Estado:

```text
Clase: Authentication, usualmente UsernamePasswordAuthenticationToken autenticado
principal: UserDetails
credentials: normalmente eliminadas/protegidas por Spring
authorities: ROLE_ADMIN, ROLE_USER, etc.
authenticated: true
```

El cambio ocurre internamente dentro de Spring Security:

```text
DaoAuthenticationProvider.authenticate(...)
        |
        v
validacion UserDetails + PasswordEncoder
        |
        v
creacion de Authentication autenticado
```

Tecnico: tu codigo crea el token no autenticado; Spring Security devuelve otro objeto `Authentication` ya autenticado.

Simple: tu codigo presenta las credenciales; Spring devuelve el sello de "credenciales validas".

---

## 6. Generacion del JWT

### Paso 1 - `AuthService` recibe autenticacion exitosa

```java
Authentication authentication = authenticationManager.authenticate(...);
UserDetails userDetails = (UserDetails) authentication.getPrincipal();
```

Tecnico: `authentication.getPrincipal()` devuelve el `UserDetails` cargado por `CustomUserDetailsService`.

Simple: se toma el usuario ya verificado.

### Paso 2 - `AuthService` llama a `JwtService`

```java
String token = jwtService.generateToken(userDetails);
```

Tecnico: se pasa el usuario autenticado, no el request original.

Simple: el token se genera con informacion confiable, no solo con lo que mando el cliente.

### Paso 3 - `JwtService` prepara claims

```java
List<String> authorities = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();
```

Claims incluidos:

| Claim | Valor |
|---|---|
| `iss` | `course-spring-security` |
| `sub` | email del usuario |
| `authorities` | lista de roles/authorities |
| `iat` | fecha de emision |
| `exp` | fecha de expiracion |

### Paso 4 - `JwtService` firma el token

```java
.signWith(getSigningKey())
```

```java
byte[] keyBytes = Decoders.BASE64.decode(secretKey);
return Keys.hmacShaKeyFor(keyBytes);
```

La configuracion viene de:

```yaml
security:
  jwt:
    secret-key: ${SECURITY_JWT_SECRET_KEY:...}
    expiration-time: ${SECURITY_JWT_EXPIRATION_TIME:3600000}
```

Tecnico: JJWT firma el token con una `SecretKey` HMAC.

Simple: el backend pone una firma que luego puede verificar para saber que el token no fue alterado.

### Paso 5 - Respuesta final

```java
return new AuthResponse(token, "Bearer", jwtService.getJwtExpiration());
```

Resultado:

```text
HTTP 200 OK
Body: token + tokenType + expiresIn
```

---

## 7. Flujo de una peticion CON JWT

Ejemplo:

```http
GET /api/v1/employee
Authorization: Bearer xxx
```

### Diagrama

```text
Cliente
   |
   v
HTTP Request con Authorization Bearer
   |
   v
SecurityFilterChain
   |
   v
JwtAuthenticationFilter.doFilterInternal(...)
   |
   v
request.getHeader("Authorization")
   |
   v
token = authHeader.substring(7)
   |
   v
JwtService.extractUsername(token)
   |
   v
CustomUserDetailsService.loadUserByUsername(username)
   |
   v
JwtService.isTokenValid(token, userDetails)
   |
   v
UsernamePasswordAuthenticationToken(userDetails, null, authorities)
   |
   v
SecurityContextHolder.getContext().setAuthentication(authToken)
   |
   v
Authorization por reglas de SecurityConfig
   |
   v
Controller
```

### Paso 1 - El filtro lee el header

```java
final String authHeader = request.getHeader("Authorization");
if(authHeader == null || !authHeader.startsWith("Bearer ")){
    filterChain.doFilter(request,response);
    return;
}
final String token = authHeader.substring(7);
```

Tecnico: si hay `Bearer`, extrae todo lo que viene despues de `"Bearer "`.

Simple: toma el JWT del header.

### Paso 2 - Extrae el username/email

```java
final String username = jwtService.extractUsername(token);
```

`JwtService` hace:

```java
return extractClaim(
        token,
        Claims::getSubject
);
```

Tecnico: lee el claim `sub` del JWT.

Simple: pregunta al token: "A que usuario perteneces?".

### Paso 3 - Carga el usuario real

```java
UserDetails userDetails = userDetailsService.loadUserByUsername(username);
```

Tecnico: vuelve a consultar el usuario actual en base de datos.

Simple: no confia solo en el token; tambien carga el usuario real.

### Paso 4 - Valida token contra usuario

```java
if(jwtService.isTokenValid(token,userDetails)){
    ...
}
```

`JwtService` valida:

```java
String username = extractUsername(token);

return username.equals(userDetails.getUsername())
        && !isTokenExpired(token);
```

Tecnico: verifica que el subject del token coincida con el usuario y que no este expirado.

Simple: confirma que el token pertenece a ese usuario y todavia sirve.

### Paso 5 - Crea `Authentication` para esta request

```java
UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails,null,userDetails.getAuthorities()
);
```

Tecnico: este constructor con authorities crea un token autenticado.

Estado:

```text
principal = userDetails
credentials = null
authorities = userDetails.getAuthorities()
authenticated = true
```

Simple: como el JWT ya fue validado, Spring recibe un objeto que representa usuario autenticado.

### Paso 6 - Guarda autenticacion en `SecurityContextHolder`

```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```

Tecnico: `SecurityContextHolder` mantiene el `SecurityContext` asociado al hilo/request actual. Dentro del contexto se guarda el `Authentication`.

Duracion: existe durante la request actual. Como el proyecto usa:

```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

Spring no guarda esa autenticacion en sesion HTTP para futuras requests. Cada request debe traer de nuevo el JWT.

Simple: para esta peticion, Spring anota: "este request pertenece a este usuario autenticado".

### Paso 7 - Autorizacion

Reglas:

```java
.requestMatchers("/api/v1/rol/**").hasRole("ADMIN")
.requestMatchers("/api/v1/employee/**").hasAnyRole("ADMIN", "USER")
.anyRequest().authenticated()
```

Tecnico: los filtros de autorizacion de Spring revisan el `Authentication` guardado en `SecurityContextHolder` y comparan sus authorities con la regla del endpoint.

Simple: ya se sabe quien eres; ahora Spring decide si tienes permiso para entrar.

---

## 8. Flujo de una peticion SIN JWT

Ejemplo:

```http
GET /api/v1/employee
```

Flujo:

```text
Request
   |
   v
SecurityFilterChain
   |
   v
JwtAuthenticationFilter
   |
   |-- No hay Authorization Bearer
   v
filterChain.doFilter(...)
   |
   v
Authorization de Spring Security
   |
   |-- /api/v1/employee/** requiere ROLE_ADMIN o ROLE_USER
   |-- SecurityContextHolder no tiene Authentication
   v
RestAuthenticationEntryPoint.commence(...)
   |
   v
HTTP 401
```

Codigo del filtro:

```java
if(authHeader == null || !authHeader.startsWith("Bearer ")){
    filterChain.doFilter(request,response);
    return;
}
```

Codigo del entry point:

```java
response.setStatus(HttpStatus.UNAUTHORIZED.value());
response.setContentType(MediaType.APPLICATION_JSON_VALUE);
response.getWriter().write("""
        {"success":false,"message":"Authentication is required or the token is invalid.","data":null}
        """);
```

Tecnico: el filtro JWT no rechaza por si mismo la request sin token. Solo no autentica. El rechazo ocurre despues, cuando la regla de autorizacion requiere un usuario autenticado.

Simple: no traer token no falla inmediatamente en el filtro; falla cuando intentas entrar a una ruta protegida.

---

## 9. JWT invalido o expirado

### Donde se detecta

En `JwtAuthenticationFilter`:

```java
try{
    final String username = jwtService.extractUsername(token);
    ...
    if(jwtService.isTokenValid(token,userDetails)){
        ...
    }
}catch (JwtException | AuthenticationException | IllegalArgumentException exception){
    SecurityContextHolder.clearContext();
    request.setAttribute("jwtAuthenticationError", exception.getMessage());
}
```

En `JwtService`, al parsear:

```java
return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
```

Y al validar expiracion:

```java
return extractExpiration(token)
        .before(Date.from(Instant.now(clock)));
```

### Que ocurre si el JWT es invalido

Tecnico:

1. `parseSignedClaims(token)` puede lanzar `JwtException`.
2. El catch limpia el contexto:

```java
SecurityContextHolder.clearContext();
```

3. La request continua por la cadena:

```java
filterChain.doFilter(request,response);
```

4. Si el endpoint es protegido, Spring no encuentra `Authentication`.
5. Se ejecuta `RestAuthenticationEntryPoint`.
6. Respuesta `401`.

Simple: si el token esta roto, mal firmado o vencido, Spring trata la request como no autenticada.

### Que ocurre si el JWT es valido pero sin rol suficiente

Ejemplo: usuario con `ROLE_USER` intenta:

```text
GET /api/v1/rol
```

La ruta requiere:

```java
.requestMatchers("/api/v1/rol/**").hasRole("ADMIN")
```

Entonces:

```text
JWT valido -> usuario autenticado -> authorities cargadas
        |
        v
No tiene ROLE_ADMIN
        |
        v
RestAccessDeniedHandler.handle(...)
        |
        v
HTTP 403
```

Tecnico: aqui si hay autenticacion, pero falla la autorizacion.

Simple: Spring sabe quien eres, pero no tienes permiso.

---

## 10. SecurityFilterChain y orden de filtros

Configuracion determinable en el proyecto:

```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

Esto significa: insertar `JwtAuthenticationFilter` antes de la posicion que Spring Security reserva para `UsernamePasswordAuthenticationFilter`.

Como ademas tienes:

```java
.formLogin(formLogin -> formLogin.disable())
```

el login form tradicional no se usa como mecanismo de login. Aun asi, la clase `UsernamePasswordAuthenticationFilter.class` sirve como punto de referencia de orden dentro de la cadena.

Orden que si se puede afirmar desde el proyecto:

```text
[FILTROS DE SPRING SECURITY ANTERIORES]
        |
        v
[FILTRO PERSONALIZADO]
JwtAuthenticationFilter
        |
        v
[POSICION DE SPRING]
UsernamePasswordAuthenticationFilter
        |
        v
[FILTROS DE SPRING SECURITY POSTERIORES]
Authorization / Exception handling / otros filtros internos
        |
        v
Controller
```

Diagrama funcional:

```text
Cliente
   |
   v
HTTP Request
   |
   v
SecurityFilterChain
   |
   v
[FILTRO DE SPRING]
Filtros internos previos no enumerados por este proyecto
   |
   v
[FILTRO PERSONALIZADO]
JwtAuthenticationFilter
   |
   |-- Sin token -> continua
   |
   |-- Con token
   |       |
   |       v
   |   JwtService
   |       |
   |       v
   |   UserDetailsService
   |       |
   |       v
   |   SecurityContextHolder.setAuthentication(...)
   |
   v
[FILTRO DE SPRING]
Posicion UsernamePasswordAuthenticationFilter
   |
   v
[FILTRO DE SPRING]
Autorizacion segun requestMatchers
   |
   v
Controller
```

No se listan todos los filtros internos exactos porque el proyecto no imprime la cadena completa en runtime. Lo verificable por codigo es la posicion relativa: `JwtAuthenticationFilter` va antes de `UsernamePasswordAuthenticationFilter`.

---

## 11. Diagrama completo final

### Login

```text
POST /api/v1/auth/login
   |
   v
SecurityFilterChain
   |
   v
JwtAuthenticationFilter
   |
   |-- request.getHeader("Authorization")
   |-- No hay Bearer
   v
filterChain.doFilter(...)
   |
   v
AuthController.login(authRequest)
   |
   v
AuthService.login(authRequest)
   |
   v
new UsernamePasswordAuthenticationToken(email, password)
   |
   v
AuthenticationManager.authenticate(token)
   |
   v
ProviderManager.authenticate(token)
   |
   v
DaoAuthenticationProvider
   |
   v
CustomUserDetailsService.loadUserByUsername(email)
   |
   v
EmployeeRepository.findByEmail(email)
   |
   v
UserDetails(username, passwordHash, authorities)
   |
   v
PasswordEncoder.matches(passwordPlano, passwordHash)
   |
   v
Authentication autenticado
   |
   v
AuthService obtiene authentication.getPrincipal()
   |
   v
JwtService.generateToken(userDetails)
   |
   v
JWT firmado
   |
   v
AuthResponse(token, "Bearer", expiresIn)
   |
   v
HTTP 200 OK
```

### Request protegida con JWT

```text
GET /api/v1/employee
Authorization: Bearer JWT
   |
   v
SecurityFilterChain
   |
   v
JwtAuthenticationFilter
   |
   v
Extrae token
   |
   v
JwtService.extractUsername(token)
   |
   v
CustomUserDetailsService.loadUserByUsername(email)
   |
   v
JwtService.isTokenValid(token, userDetails)
   |
   v
new UsernamePasswordAuthenticationToken(userDetails, null, authorities)
   |
   v
SecurityContextHolder.getContext().setAuthentication(authToken)
   |
   v
Spring Security Authorization
   |
   |-- /api/v1/employee/** requiere ROLE_ADMIN o ROLE_USER
   |
   v
EmployeeController
```

---

## 12. Tabla resumen

| Orden | Componente | Metodo | Lo ejecuta | Funcion |
| ----: | ---------- | ------ | ---------- | ------- |
| 1 | `SecurityFilterChain` | `securityFilterChain(...)` | Spring Security | Define como se protege cada request. |
| 2 | `JwtAuthenticationFilter` | `doFilterInternal(...)` | Spring Security | Busca `Authorization: Bearer` y autentica por JWT si existe. |
| 3 | `AuthController` | `login(...)` | Spring MVC / mi codigo | Recibe login y llama al servicio. |
| 4 | `AuthService` | `login(...)` | Mi codigo | Crea `UsernamePasswordAuthenticationToken` y llama al manager. |
| 5 | `AuthenticationManager` | `authenticate(...)` | Mi codigo llama / Spring ejecuta | Entrada principal de autenticacion. |
| 6 | `ProviderManager` | `authenticate(...)` | Interno Spring Security | Busca un provider compatible. |
| 7 | `DaoAuthenticationProvider` | `authenticate(...)` | Interno Spring Security | Autentica usuario/password. |
| 8 | `CustomUserDetailsService` | `loadUserByUsername(...)` | Spring llama / mi codigo ejecuta | Busca usuario por email y crea `UserDetails`. |
| 9 | `EmployeeRepository` | `findByEmail(...)` | Mi codigo | Consulta usuario en base de datos. |
| 10 | `PasswordEncoder` | `matches(...)` | Spring Security | Compara password plana con hash. |
| 11 | `Authentication` | objeto resultado | Spring Security | Representa usuario autenticado. |
| 12 | `JwtService` | `generateToken(...)` | Mi codigo | Genera y firma JWT. |
| 13 | `SecurityContextHolder` | `setAuthentication(...)` | Mi filtro | Guarda el usuario autenticado durante una request con JWT. |
| 14 | `RestAuthenticationEntryPoint` | `commence(...)` | Spring Security | Devuelve `401` si falta autenticacion. |
| 15 | `RestAccessDeniedHandler` | `handle(...)` | Spring Security | Devuelve `403` si falta permiso. |

---

## 13. Resumen para estudiar

Autenticacion responde: "Quien eres?".

En el login, tu codigo recibe email/password, crea un `UsernamePasswordAuthenticationToken` no autenticado y se lo entrega a `AuthenticationManager`. Spring Security usa internamente `ProviderManager`, elige `DaoAuthenticationProvider`, llama a tu `CustomUserDetailsService`, obtiene un `UserDetails`, compara password con `BCryptPasswordEncoder` y devuelve un `Authentication` autenticado. Luego tu `AuthService` toma el `UserDetails` y `JwtService` genera un JWT firmado.

Autorizacion responde: "Tienes permiso para entrar aqui?".

En requests posteriores, el cliente manda `Authorization: Bearer JWT`. `JwtAuthenticationFilter` extrae y valida el token, carga el usuario, crea un `Authentication` autenticado y lo guarda en `SecurityContextHolder`. Despues Spring Security mira las reglas de `SecurityConfig`: `/api/v1/rol/**` necesita `ROLE_ADMIN`, `/api/v1/employee/**` necesita `ROLE_ADMIN` o `ROLE_USER`.

La diferencia clave:

```text
LOGIN
email/password -> AuthenticationManager -> PasswordEncoder -> JWT

REQUEST CON JWT
JWT -> JwtAuthenticationFilter -> SecurityContextHolder -> autorizacion -> controller
```

Si no hay JWT en una ruta protegida, el filtro deja continuar pero Spring Security rechaza con `401`. Si el JWT es valido pero el rol no alcanza, Spring Security rechaza con `403`.

