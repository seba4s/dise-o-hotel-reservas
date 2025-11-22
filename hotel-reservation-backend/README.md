# Hotel Reservation Backend

Backend del sistema de reservas de hotel (Spring Boot + MongoDB + JWT + Swagger).

## Requisitos
- Java 17
- Maven 3.9+
- MongoDB 6+

## Ejecutar
```bash
mvn -q -f hotel-reservation-backend/pom.xml clean spring-boot:run
```
Endpoint de verificación:
```bash
curl http://localhost:8080/health
```
Swagger UI:
http://localhost:8080/swagger-ui/index.html

## Estructura inicial
```
hotel-reservation-backend/
  pom.xml
  src/main/java/com/hotel/reservation/
    HotelReservationApplication.java
    config/
    controller/
    service/ (pendiente completar)
    repository/ (pendiente completar)
    model/ (pendiente completar)
  src/main/resources/
    application.yml
```