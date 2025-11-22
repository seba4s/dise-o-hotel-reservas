# Hotel Reservation Backend

Backend del sistema de reservas de hotel (Spring Boot + MongoDB + JWT + Swagger).

## Requisitos
- Java 17
- Maven 3.9+
- MongoDB 6+

## Ejecutar
```bash
mvn clean package -DskipTests
java -jar target/hotel-reservation-backend-0.0.1-SNAPSHOT.jar
```

O con Maven:
```bash
mvn spring-boot:run
```

## Endpoints Principales

### Salud / Health Check
```bash
curl http://localhost:8080/health
curl http://localhost:8080/actuator/health
```

### Documentación API (Swagger)
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/v3/api-docs

### Autenticación (Públicos)
- `POST /auth/register` - Registro de usuario
- `POST /auth/login` - Login (retorna JWT tokens)
- `POST /auth/refresh` - Renovar token JWT
- `POST /auth/logout` - Cerrar sesión

### Disponibilidad (Público)
- `POST /availability/search` - Buscar habitaciones disponibles
- `GET /availability/rooms/{id}` - Detalles de habitación
- `POST /availability/check` - Verificar disponibilidad específica

### Reservas (Autenticado)
- `POST /reservations` - Crear nueva reserva
- `GET /reservations/{id}` - Obtener detalles de reserva
- `GET /reservations/confirmation/{number}` - Buscar por número de confirmación
- `GET /reservations/user/{userId}` - Reservas de un usuario
- `PUT /reservations/{id}` - Actualizar reserva
- `DELETE /reservations/{id}` - Cancelar reserva

### Check-in (STAFF/ADMIN)
- `POST /checkin` - Procesar check-in
- `GET /checkin/today` - Check-ins de hoy
- `GET /checkin/search` - Buscar reservas para check-in
- `GET /checkin/history` - Historial de check-ins

### Check-out (STAFF/ADMIN)
- `POST /checkout` - Procesar check-out
- `GET /checkout/today` - Check-outs de hoy
- `GET /checkout/history` - Historial de check-outs
- `POST /checkout/calculate` - Calcular total de check-out

### Administración (ADMIN)
- Endpoints administrativos protegidos con rol ADMIN

## Seguridad

El sistema utiliza JWT (JSON Web Tokens) para autenticación:

1. **Registrarse**: `POST /auth/register`
2. **Login**: `POST /auth/login` - retorna `accessToken` y `refreshToken`
3. **Usar token**: Agregar header `Authorization: Bearer <accessToken>` en requests protegidos
4. **Renovar token**: `POST /auth/refresh` con token expirado

### Roles
- `CLIENT` - Usuario cliente (puede hacer reservas)
- `STAFF` - Personal del hotel (puede hacer check-in/check-out)
- `ADMIN` - Administrador (acceso completo)
- `AUDITOR` - Auditor (solo lectura de logs)

## Estructura del Proyecto
```
hotel-reservation-backend/
  src/main/java/com/hotel/reservation/
    config/           - Configuración (Security, JWT, Mongo, CORS, Swagger)
    controller/       - REST Controllers
    dto/
      request/        - DTOs de entrada
      response/       - DTOs de salida
    exception/        - Excepciones personalizadas y GlobalExceptionHandler
    mapper/           - MapStruct mappers
    model/            - Entidades MongoDB (User, Room, Reservation, AuditLog)
    repository/       - Repositorios MongoDB
    service/          - Lógica de negocio
    util/             - Utilidades (JWT, Date, Validation, etc.)
  src/main/resources/
    application.yml   - Configuración de la aplicación
```

## Configuración

Editar `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/hotel_reservations
      database: hotel_reservations

jwt:
  secret: your-secret-key-here
  expiration: 86400000        # 24 horas
  refresh-expiration: 604800000  # 7 días
```

## Ejemplos de Uso

### Registrar usuario
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cliente1",
    "email": "cliente@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "firstName": "Juan",
    "lastName": "Pérez",
    "phone": "+57 300 1234567",
    "country": "Colombia",
    "role": "CLIENT"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cliente1",
    "password": "password123"
  }'
```

### Buscar disponibilidad
```bash
curl -X POST http://localhost:8080/availability/search \
  -H "Content-Type: application/json" \
  -d '{
    "checkInDate": "2025-12-01",
    "checkOutDate": "2025-12-05",
    "adults": 2,
    "children": 1,
    "roomType": "Deluxe"
  }'
```

### Crear reserva (con JWT)
```bash
curl -X POST http://localhost:8080/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "roomId": "room123",
    "checkInDate": "2025-12-01",
    "checkOutDate": "2025-12-05",
    "adults": 2,
    "children": 1,
    "specialRequests": "Vista al mar",
    "paymentMethod": "CREDIT_CARD"
  }'
```

## Notas de Desarrollo

- Todas las fechas en formato ISO-8601 (YYYY-MM-DD)
- Todas las respuestas en JSON
- Manejo de errores consistente con códigos HTTP estándar
- Auditoría automática de cambios con MongoDB auditing
- Validación de entrada con Bean Validation (Jakarta)
- MapStruct para mapping eficiente de DTOs