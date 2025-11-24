# Hotel Reservation System - Postman Collection Guide

## 📦 Archivos Incluidos

1. **Hotel-Reservation-Complete-APIs.postman_collection.json** - Colección completa con todas las APIs
2. **Hotel-Local-Environment.postman_environment.json** - Variables de entorno para desarrollo local

---

## 🚀 Cómo Importar en Postman

### Paso 1: Importar la Colección
1. Abre Postman
2. Haz clic en **"Import"** (esquina superior izquierda)
3. Arrastra el archivo `Hotel-Reservation-Complete-APIs.postman_collection.json` o haz clic en "Upload Files"
4. Haz clic en **"Import"**

### Paso 2: Importar el Environment
1. Haz clic en el ícono de engranaje ⚙️ (esquina superior derecha)
2. Haz clic en **"Import"**
3. Arrastra el archivo `Hotel-Local-Environment.postman_environment.json`
4. Haz clic en **"Import"**

### Paso 3: Activar el Environment
1. En el dropdown de environments (esquina superior derecha)
2. Selecciona **"Hotel Reservation - Local"**

---

## 🔧 Ejecutar seed de habitaciones (opcional pero recomendado)

Si la consulta de disponibilidad devuelve un arreglo vacío, inserta habitaciones de prueba en la base de datos local/Atlas ejecutando el script incluido `seed_rooms_mongosh.js`.

1. Asegúrate de tener `mongosh` instalado.
2. Ejecuta desde una terminal (reemplaza la URI si usas otra):

```powershell
mongosh "mongodb+srv://jlopezbenavides73_db_user:2945SebAs@cluster0.yapzpyl.mongodb.net/hotel_reservations" postman-collections/seed_rooms_mongosh.js
```

3. Verás un mensaje "Seed finished" si las habitaciones se insertaron correctamente.

Nota: el script hace upsert por `roomNumber`, así que puede ejecutarse varias veces sin duplicar documentos.

---

## 📋 Orden Recomendado para Probar las APIs

### 1️⃣ **Primero: Health Check**
```
GET /api/health
```
- ✅ No requiere autenticación
- Verifica que el servidor esté corriendo

---

### 2️⃣ **Autenticación**

#### Registrar Usuario Cliente
```
POST /api/auth/register
```
- Crea un usuario con rol `CLIENT`
- Guarda automáticamente el token en `{{client_token}}`

#### Registrar Usuario Staff
```
POST /api/auth/register
```
- Crea un usuario con rol `STAFF`
- Guarda automáticamente el token en `{{staff_token}}`

#### Login
```
POST /api/auth/login
```
- Si ya registraste usuarios, puedes hacer login
- Los tokens se guardan automáticamente

---

### 3️⃣ **Consultar Disponibilidad**

```
GET /api/availability/quick-search
```
- ✅ No requiere autenticación
- Prueba los 3 ejemplos incluidos:
  - Single Room (1 huésped)
  - Double Room (2 huéspedes)
  - Suite (4 huéspedes)

---

### 4️⃣ **Crear Reservación (Como Cliente)**

```
POST /api/reservations
```
- 🔐 Requiere token de CLIENT
- **IMPORTANTE**: Reemplaza `"ROOM_ID_HERE"` con un ID real obtenido de la consulta de disponibilidad
- El ID de la reservación se guarda automáticamente en `{{reservation_id}}`

---

### 5️⃣ **Ver Mis Reservaciones**

```
GET /api/reservations/my-reservations
```
- 🔐 Requiere token de CLIENT
- Lista todas las reservaciones del usuario autenticado

---

### 6️⃣ **Obtener Reservación por ID**

```
GET /api/reservations/{{reservation_id}}
```
- 🔐 Requiere token de CLIENT
- Usa el `{{reservation_id}}` guardado automáticamente

---

### 7️⃣ **Actualizar Reservación**

```
PUT /api/reservations/{{reservation_id}}
```
- 🔐 Requiere token de CLIENT
- Modifica fechas, número de huéspedes o solicitudes especiales

---

### 8️⃣ **Process Check-In (Como Staff)**

```
POST /api/checkin/process
```
- 🔐 Requiere token de STAFF
- Usa el `{{reservation_id}}` de una reservación confirmada
- Cambia el estado a "CHECKED_IN"

---

### 9️⃣ **Ver Check-Ins de Hoy**

```
GET /api/checkin/today
```
- 🔐 Requiere token de STAFF
- Lista todos los check-ins del día actual

---

### 🔟 **Process Check-Out (Como Staff)**

```
POST /api/checkout/process
```
- 🔐 Requiere token de STAFF
- Finaliza la estadía del huésped
- Métodos de pago: `CREDIT_CARD`, `DEBIT_CARD`, `CASH`, `TRANSFER`

---

### 1️⃣1️⃣ **Ver Check-Outs de Hoy**

```
GET /api/checkout/today
```
- 🔐 Requiere token de STAFF
- Lista todos los check-outs del día actual

---

### 1️⃣2️⃣ **Cancelar Reservación**

```
DELETE /api/reservations/{{reservation_id}}/cancel
```
- 🔐 Requiere token de CLIENT
- Cancela una reservación existente

---

### 1️⃣3️⃣ **Ver Todas las Reservaciones (Como Staff)**

```
GET /api/reservations?page=0&size=10
```
- 🔐 Requiere token de STAFF
- Lista todas las reservaciones del sistema (paginado)

---

## 🔑 Variables de Entorno

Las siguientes variables se guardan automáticamente:

| Variable | Descripción |
|----------|-------------|
| `base_url` | URL base del servidor (default: `http://localhost:8080`) |
| `client_token` | Token JWT del usuario CLIENT |
| `staff_token` | Token JWT del usuario STAFF |
| `client_user_id` | ID del usuario CLIENT |
| `staff_user_id` | ID del usuario STAFF |
| `reservation_id` | ID de la última reservación creada |

---

## 📝 Notas Importantes

### Roles de Usuario
- **CLIENT**: Usuarios regulares que pueden hacer reservaciones
- **STAFF**: Personal del hotel que puede hacer check-in/check-out
- **ADMIN**: Administradores (tienen los mismos permisos que STAFF)
- **AUDITOR**: Auditor del sistema (solo lectura)

### Tipos de Habitación
- `SINGLE` - Habitación individual
- `DOUBLE` - Habitación doble
- `SUITE` - Suite
- `DELUXE` - Habitación deluxe

### Estados de Reservación
- `PENDING` - Pendiente de confirmación
- `CONFIRMED` - Confirmada
- `CHECKED_IN` - Cliente registrado (check-in realizado)
- `CHECKED_OUT` - Cliente desregistrado (check-out realizado)
- `CANCELLED` - Cancelada

### Métodos de Pago
- `CREDIT_CARD` - Tarjeta de crédito
- `DEBIT_CARD` - Tarjeta de débito
- `CASH` - Efectivo
- `TRANSFER` - Transferencia bancaria

---

## 🐛 Troubleshooting

### Token Expirado (401 Unauthorized)
Si recibes error 401, el token expiró:
1. Ejecuta nuevamente el request de **Login Client** o **Login Staff**
2. El token se actualizará automáticamente

### Room Not Available (409 Conflict)
Si al crear una reservación recibes error 409:
1. La habitación no está disponible para esas fechas
2. Prueba con otras fechas o tipo de habitación

### Forbidden (403)
Si recibes error 403:
1. Verifica que estés usando el token correcto (client vs staff)
2. Algunos endpoints solo están disponibles para STAFF/ADMIN

---

## ✅ Flujo Completo de Prueba

1. ✓ Health Check
2. ✓ Register Client User
3. ✓ Register Staff User
4. ✓ Check Availability - Single Room
5. ✓ Create Reservation (usa el Room ID de la respuesta anterior)
6. ✓ Get My Reservations
7. ✓ Process Check-In (con token de staff)
8. ✓ Get Today's Check-Ins
9. ✓ Process Check-Out (con token de staff)
10. ✓ Get Today's Check-Outs

---

## 📞 Soporte

Si encuentras algún problema:
1. Verifica que el servidor esté corriendo en `http://localhost:8080`
2. Asegúrate de haber importado y activado el environment
3. Revisa que los tokens estén actualizados

---

**¡Feliz Testing! 🎉**
